package mthree.stocksimulator.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.dto.ApiDtos.OwnedStockDto;
import mthree.stocksimulator.dto.ApiDtos.PortfolioDto;
import mthree.stocksimulator.dto.ApiDtos.StockChangeDto;
import mthree.stocksimulator.dto.ApiDtos.TradeRequest;
import mthree.stocksimulator.dto.ApiDtos.TradeResult;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;
import mthree.stocksimulator.model.User;
import mthree.stocksimulator.service.InvalidOrderException;
import mthree.stocksimulator.service.SimService;
import mthree.stocksimulator.service.UserService;
import org.springframework.http.ResponseEntity;
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
 * SimServiceImpl as shared state, so all users share one timeline.
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
        List<StockPriceSnapshot> snapshots = simService.getStocksWithPriceChange();
        List<StockChangeDto> out = new ArrayList<>();
        for (StockPriceSnapshot sps : snapshots) {
            Stock s = sps.getStock();
            BigDecimal price = sps.getCurrentPrice();
            out.add(new StockChangeDto(
                    s.getSid(),
                    s.getStockCode(),
                    s.getStockName(),
                    price,
                    pctChange(price, sps.getPrevDayPrice()),
                    pctChange(price, sps.getPrev7Price()),
                    pctChange(price, sps.getPrev30Price()),
                    pctChange(price, sps.getPrev1YPrice())));
        }
        return out;
    }

    /** Stocks a given user currently owns, with shares and value. */
    @GetMapping("/stocks/owned/{uid}")
    public List<OwnedStockDto> owned(@PathVariable int uid) {
        // Build a sid -> snapshot map from the market data for current prices
        Map<Integer, StockPriceSnapshot> priceMap = new HashMap<>();
        for (StockPriceSnapshot sps : simService.getStocksWithPriceChange()) {
            priceMap.put(sps.getStock().getSid(), sps);
        }

        Map<Integer, Integer> ownedMap = simService.getAllOwnedStocks(uid);
        List<OwnedStockDto> out = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : ownedMap.entrySet()) {
            int sid = entry.getKey();
            int shares = entry.getValue();
            if (shares <= 0) continue;

            StockPriceSnapshot sps = priceMap.get(sid);
            if (sps == null) continue;

            Stock s = sps.getStock();
            BigDecimal price = sps.getCurrentPrice();
            BigDecimal value = price.multiply(BigDecimal.valueOf(shares))
                    .setScale(2, RoundingMode.HALF_UP);
            out.add(new OwnedStockDto(
                    sid, s.getStockCode(), s.getStockName(),
                    price, shares, value));
        }
        return out;
    }

    /** Price history for one stock up to the current sim date (oldest first).
     *  Optional ?days=N limits to the most recent N calendar days. */
    @GetMapping("/stocks/{sid}/history")
    public Map<String, BigDecimal> history(
            @PathVariable int sid,
            @RequestParam(required = false) Integer days) {
        if (days != null && days > 0) {
            return simService.getPriceHistory(sid, days);
        }
        return simService.getPriceHistory(sid);
    }

    @PostMapping("/stocks/buy")
    public TradeResult buy(@RequestBody TradeRequest req) {
        try {
            // Get the correct simulation-date price from market snapshots
            BigDecimal price = getSnapshotPrice(req.sid());
            if (price == null) {
                return new TradeResult(false, "No price data available for this stock.");
            }
            BigDecimal cost = price.multiply(BigDecimal.valueOf(req.quantity()));
            // Deduct balance first (throws InvalidOrderException if insufficient funds)
            simService.updateUserBal(req.uid(), cost.negate());
            // Then add the shares
            simService.buyStock(req.uid(), req.sid(), req.quantity());
            return new TradeResult(true,
                    "Successfully purchased " + req.quantity() + " share(s) for $" + cost.setScale(2, RoundingMode.HALF_UP));
        } catch (InvalidOrderException e) {
            return new TradeResult(false, e.getMessage());
        } catch (Exception e) {
            return new TradeResult(false, "Trade failed: " + e.getMessage());
        }
    }

    @PostMapping("/stocks/sell")
    public TradeResult sell(@RequestBody TradeRequest req) {
        try {
            // Check shares and remove them (throws InvalidOrderException if insufficient)
            simService.sellStock(req.uid(), req.sid(), req.quantity());
            // Calculate proceeds using the correct simulation-date price
            BigDecimal price = getSnapshotPrice(req.sid());
            BigDecimal proceeds = price != null
                    ? price.multiply(BigDecimal.valueOf(req.quantity()))
                    : BigDecimal.ZERO;
            // Add proceeds to balance
            simService.updateUserBal(req.uid(), proceeds);
            return new TradeResult(true,
                    "Successfully sold " + req.quantity() + " share(s) for $" + proceeds.setScale(2, RoundingMode.HALF_UP));
        } catch (InvalidOrderException e) {
            return new TradeResult(false, e.getMessage());
        } catch (Exception e) {
            return new TradeResult(false, "Trade failed: " + e.getMessage());
        }
    }

    /** Look up the current simulation-date price for a stock from the market snapshots. */
    private BigDecimal getSnapshotPrice(int sid) {
        for (StockPriceSnapshot sps : simService.getStocksWithPriceChange()) {
            if (sps.getStock().getSid() == sid) {
                return sps.getCurrentPrice();
            }
        }
        return null;
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

        // Use market snapshots for current prices to avoid N+1 queries
        Map<Integer, BigDecimal> priceMap = new HashMap<>();
        for (StockPriceSnapshot sps : simService.getStocksWithPriceChange()) {
            priceMap.put(sps.getStock().getSid(), sps.getCurrentPrice());
        }

        BigDecimal stockValue = BigDecimal.ZERO;
        Map<Integer, Integer> ownedMap = simService.getAllOwnedStocks(uid);
        for (Map.Entry<Integer, Integer> entry : ownedMap.entrySet()) {
            int shares = entry.getValue();
            BigDecimal price = priceMap.get(entry.getKey());
            if (shares > 0 && price != null) {
                stockValue = stockValue.add(price.multiply(BigDecimal.valueOf(shares)));
            }
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