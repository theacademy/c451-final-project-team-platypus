package mthree.stocksimulator.web;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import mthree.stocksimulator.dto.ApiDtos.OwnedStockDto;
import mthree.stocksimulator.dto.ApiDtos.PortfolioDto;
import mthree.stocksimulator.dto.ApiDtos.StockChangeDto;
import mthree.stocksimulator.dto.ApiDtos.TradeRequest;
import mthree.stocksimulator.dto.ApiDtos.TradeResult;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.User;
import mthree.stocksimulator.service.SimServiceImpl;
import mthree.stocksimulator.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockRestControllerTest {

    @Mock private SimServiceImpl simService;
    @Mock private UserServiceImpl userService;

    private StockRestController controller;

    @BeforeEach
    void setUp() {
        controller = new StockRestController(simService, userService);
    }

    // ---- Market ----

    @Test
    void market_returnsStockList() {
        Stock[] row = makeStockRow(1, "AAPL", "150.00", "148.00", "145.00", "140.00", "120.00");
        when(simService.getStocksWithPriceChange()).thenReturn(Collections.singletonList(row));

        List<StockChangeDto> result = controller.market();

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).stockCode());
        assertEquals(new BigDecimal("150.00"), result.get(0).price());
        assertNotNull(result.get(0).change1d());
    }

    @Test
    void market_returnsEmptyListWhenNoStocks() {
        when(simService.getStocksWithPriceChange()).thenReturn(Collections.emptyList());

        List<StockChangeDto> result = controller.market();

        assertTrue(result.isEmpty());
    }

    @Test
    void market_handlesNullPreviousPrices() {
        Stock[] row = makeStockRow(1, "AAPL", "150.00", null, null, null, null);
        when(simService.getStocksWithPriceChange()).thenReturn(Collections.singletonList(row));

        List<StockChangeDto> result = controller.market();

        assertEquals(1, result.size());
        assertNull(result.get(0).change1d());
        assertNull(result.get(0).change7d());
    }

    // ---- Owned stocks ----

    @Test
    void owned_returnsPositionsWithValue() {
        Stock stock = makeStock(1, "AAPL", "150.00", 10);
        when(simService.getOwnedStocks(1)).thenReturn(List.of(stock));

        List<OwnedStockDto> result = controller.owned(1);

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).stockCode());
        assertEquals(10, result.get(0).shares());
        assertEquals(new BigDecimal("1500.00"), result.get(0).value());
    }

    @Test
    void owned_returnsEmptyWhenNoStocks() {
        when(simService.getOwnedStocks(1)).thenReturn(Collections.emptyList());

        List<OwnedStockDto> result = controller.owned(1);

        assertTrue(result.isEmpty());
    }

    // ---- Buy ----

    @Test
    void buy_returnsSuccessResult() {
        when(simService.buyStock(1, 1, 5)).thenReturn("Successfully purchased 5 share(s)");

        TradeResult result = controller.buy(new TradeRequest(1, 1, 5));

        assertTrue(result.success());
        assertTrue(result.message().startsWith("Successfully"));
    }

    @Test
    void buy_returnsFailureOnInsufficientFunds() {
        when(simService.buyStock(1, 1, 5)).thenReturn("Insufficient funds. Cost: 750, Balance: 100");

        TradeResult result = controller.buy(new TradeRequest(1, 1, 5));

        assertFalse(result.success());
    }

    // ---- Sell ----

    @Test
    void sell_returnsSuccessResult() {
        when(simService.sellStock(1, 1, 3)).thenReturn("Successfully sold 3 share(s)");

        TradeResult result = controller.sell(new TradeRequest(1, 1, 3));

        assertTrue(result.success());
    }

    @Test
    void sell_returnsFailureOnInsufficientShares() {
        when(simService.sellStock(1, 1, 100)).thenReturn("Insufficient shares. Owned: 5");

        TradeResult result = controller.sell(new TradeRequest(1, 1, 100));

        assertFalse(result.success());
    }

    // ---- Sim state ----

    @Test
    void state_returnsPortfolioWithHoldings() {
        User user = makeUser(1, "alice", "90000.00");
        Stock stock = makeStock(1, "AAPL", "150.00", 10);
        when(userService.getUser(1)).thenReturn(user);
        when(simService.getOwnedStocks(1)).thenReturn(List.of(stock));
        when(simService.getDate()).thenReturn("2010-01-04");
        when(simService.isSimulationOver()).thenReturn(false);

        PortfolioDto result = controller.state(1);

        assertEquals("2010-01-04", result.date());
        assertEquals(new BigDecimal("90000.00"), result.cash());
        assertEquals(new BigDecimal("1500.00"), result.stockValue());
        assertEquals(new BigDecimal("91500.00"), result.total());
        assertFalse(result.simulationOver());
    }

    @Test
    void state_returnsZeroStockValueWhenNoHoldings() {
        User user = makeUser(1, "alice", "100000.00");
        when(userService.getUser(1)).thenReturn(user);
        when(simService.getOwnedStocks(1)).thenReturn(Collections.emptyList());
        when(simService.getDate()).thenReturn("2010-01-04");
        when(simService.isSimulationOver()).thenReturn(false);

        PortfolioDto result = controller.state(1);

        assertEquals(new BigDecimal("0.00"), result.stockValue());
        assertEquals(new BigDecimal("100000.00"), result.total());
    }

    // ---- Advance ----

    @Test
    void advance_movesClockAndReturnsPortfolio() {
        User user = makeUser(1, "alice", "100000.00");
        when(userService.getUser(1)).thenReturn(user);
        when(simService.getOwnedStocks(1)).thenReturn(Collections.emptyList());
        when(simService.getDate()).thenReturn("2010-01-11");
        when(simService.isSimulationOver()).thenReturn(false);

        PortfolioDto result = controller.advance(5, 1);

        assertEquals("2010-01-11", result.date());
        verify(simService).advanceTime(5);
    }

    // ---- Restart ----

    @Test
    void restart_resetsAndReturnsPortfolio() {
        User user = makeUser(1, "alice", "100000.00");
        when(userService.getUser(1)).thenReturn(user);
        when(simService.getOwnedStocks(1)).thenReturn(Collections.emptyList());
        when(simService.getDate()).thenReturn("2010-01-04");
        when(simService.isSimulationOver()).thenReturn(false);

        PortfolioDto result = controller.restart(1, new BigDecimal("100000.00"));

        assertEquals("2010-01-04", result.date());
        assertEquals(new BigDecimal("0.00"), result.stockValue());
        verify(simService).restartSimulation(eq(1), any(BigDecimal.class));
    }

    // ---- History ----

    @Test
    void history_withDays_passesParam() {
        when(simService.getPriceHistory(1, 30)).thenReturn(Collections.emptyList());

        controller.history(1, 30);

        verify(simService).getPriceHistory(1, 30);
        verify(simService, never()).getPriceHistory(1);
    }

    @Test
    void history_withoutDays_fetchesAll() {
        when(simService.getPriceHistory(1)).thenReturn(Collections.emptyList());

        controller.history(1, null);

        verify(simService).getPriceHistory(1);
    }

    // ---- Helpers ----

    private User makeUser(int uid, String name, String balance) {
        User user = new User();
        user.setUid(uid);
        user.setUserName(name);
        user.setAccountBal(new BigDecimal(balance));
        return user;
    }

    private Stock makeStock(int sid, String code, String price, int owned) {
        Stock s = new Stock();
        s.setSid(sid);
        s.setStockCode(code);
        s.setStockName(code);
        s.setStockPrice(new BigDecimal(price));
        s.setOwnedShares(owned);
        return s;
    }

    /** Build a 5-element Stock[] row as returned by getStocksWithPriceChange. */
    private Stock[] makeStockRow(int sid, String code,
                                  String current, String prev1d,
                                  String prev7d, String prev30d,
                                  String prev1y) {
        Stock[] row = new Stock[5];
        String[] prices = {current, prev1d, prev7d, prev30d, prev1y};
        for (int i = 0; i < 5; i++) {
            Stock s = new Stock();
            s.setSid(sid);
            s.setStockCode(code);
            s.setStockName(code);
            s.setStockPrice(prices[i] != null ? new BigDecimal(prices[i]) : null);
            row[i] = s;
        }
        return row;
    }
}