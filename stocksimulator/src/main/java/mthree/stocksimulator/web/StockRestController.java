package mthree.stocksimulator.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import mthree.stocksimulator.dto.ApiDtos.OwnedStockDto;
import mthree.stocksimulator.dto.ApiDtos.PortfolioDto;
import mthree.stocksimulator.dto.ApiDtos.StockChangeDto;
import mthree.stocksimulator.dto.ApiDtos.TradeRequest;
import mthree.stocksimulator.dto.ApiDtos.TradeResult;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.User;
import mthree.stocksimulator.service.UserService;
import mthree.stocksimulator.service.SimService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Market data, trading, and simulation-clock endpoints.
 *
 * NOTE: the simulation clock (current date) is GLOBAL — it lives in
 * SimServiceImpl as shared state, so all users share one timeline. That mirrors
 * the original CLI design; per-user clocks would need a schema/service change.
 */
@RestController
@RequestMapping("/api")
public class StockRestController {

    private final SimService simService;
    private final UserService userService;

    public StockRestController(SimService simService, UserService userService) {
        this.simService = simService;
        this.userService = userService;
    }

    /** Current market: every stock with price and % change over 1d/7d/30d/1y. */
    @GetMapping("/stocks")
    public List<StockChangeDto> market() {
        List<Stock[]> rows = simService.getStocksWithPriceChange();
        List<StockChangeDto> out = new ArrayList<>();
        for (Stock[] r : rows) {
            Stock cur = r[0];
            out.add(new StockChangeDto(
                    cur.getSid(),
                    cur.getStockCode(),
                    cur.getStockName(),
                    cur.getStockPrice(),
                    pctChange(cur.getStockPrice(), r[1].getStockPrice()),
                    pctChange(cur.getStockPrice(), r[2].getStockPrice()),
                    pctChange(cur.getStockPrice(), r[3].getStockPrice()),
                    pctChange(cur.getStockPrice(), r[4].getStockPrice())));
        }
        return out;
    }

    /** Stocks a given user currently owns, with shares and value. */
    @GetMapping("/stocks/owned/{uid}")
    public List<OwnedStockDto> owned(@PathVariable int uid) {
        List<OwnedStockDto> out = new ArrayList<>();
        for (Stock s : simService.getOwnedStocks(uid)) {
            int shares = s.getOwnedShares();
            BigDecimal value = s.getStockPrice()
                    .multiply(BigDecimal.valueOf(shares))
                    .setScale(2, RoundingMode.HALF_UP);
            out.add(new OwnedStockDto(
                    s.getSid(), s.getStockCode(), s.getStockName(),
                    s.getStockPrice(), shares, value));
        }
        return out;
    }

    /** Price history for one stock up to the current sim date (oldest first).
     *  Optional ?days=N limits to the most recent N calendar days. */
    @GetMapping("/stocks/{sid}/history")
    public List<java.util.Map<String, Object>> history(
            @PathVariable int sid,
            @RequestParam(required = false) Integer days) {
        if (days != null && days > 0) {
            return simService.getPriceHistory(sid, days);
        }
        return simService.getPriceHistory(sid);
    }

    @PostMapping("/stocks/buy")
    public TradeResult buy(@RequestBody TradeRequest req) {
        String msg = simService.buyStock(req.uid(), req.sid(), req.quantity());
        return new TradeResult(msg.startsWith("Successfully"), msg);
    }

    @PostMapping("/stocks/sell")
    public TradeResult sell(@RequestBody TradeRequest req) {
        String msg = simService.sellStock(req.uid(), req.sid(), req.quantity());
        return new TradeResult(msg.startsWith("Successfully"), msg);
    }

    /** Advance the global simulation clock by N trading days. */
    @PostMapping("/sim/advance")
    public PortfolioDto advance(@RequestParam(defaultValue = "1") int days,
                                @RequestParam int uid) {
        simService.advanceTime(days);
        return buildPortfolio(uid);
    }

    /** Current portfolio snapshot for a user (cash + holdings value + date). */
    @GetMapping("/sim/state/{uid}")
    public PortfolioDto state(@PathVariable int uid) {
        return buildPortfolio(uid);
    }

    /** Restart the simulation: reset clock to day 0 and reset user portfolio. */
    @PostMapping("/sim/restart")
    public PortfolioDto restart(@RequestParam int uid,
                                @RequestParam(defaultValue = "100000.00") BigDecimal balance) {
        simService.restartSimulation(uid, balance);
        return buildPortfolio(uid);
    }

    // ---- helpers ----

    private PortfolioDto buildPortfolio(int uid) {
        User user = userService.getUser(uid);
        BigDecimal stockValue = BigDecimal.ZERO;
        for (Stock s : simService.getOwnedStocks(uid)) {
            int shares = s.getOwnedShares();
            stockValue = stockValue.add(
                    s.getStockPrice().multiply(BigDecimal.valueOf(shares)));
        }
        stockValue = stockValue.setScale(2, RoundingMode.HALF_UP);
        BigDecimal cash = user.getAccountBal();
        return new PortfolioDto(
                simService.getDate(),
                cash,
                stockValue,
                cash.add(stockValue).setScale(2, RoundingMode.HALF_UP),
                simService.isSimulationOver());
    }

    /** Percentage change from previous to current; null when no comparison price. */
    private BigDecimal pctChange(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}