package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import mthree.stocksimulator.dao.StockDao;
import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimServiceImplTest {

    @Mock private StockDao stockDao;
    @Mock private UserDao userDao;

    private SimServiceImpl simService;

    @BeforeEach
    void setUp() {
        simService = new SimServiceImpl(stockDao, userDao, WebClient.builder());
        // Seed trading days so the service doesn't try to load from DB
        simService.tradingDays = new ArrayList<>(List.of(
                "2010-01-04", "2010-01-05", "2010-01-06",
                "2010-01-07", "2010-01-08"));
        simService.currentIndex = 0;
    }

    // ---- Date & time advancement ----

    @Test
    void getDate_returnsFirstTradingDay() {
        assertEquals("2010-01-04", simService.getDate());
    }

    @Test
    void advanceTime_movesForward() {
        simService.advanceTime(2);
        assertEquals("2010-01-06", simService.getDate());
    }

    @Test
    void advanceTime_clampsToLastDay() {
        simService.advanceTime(100);
        assertEquals("2010-01-08", simService.getDate());
    }

    @Test
    void isSimulationOver_falseAtStart() {
        assertFalse(simService.isSimulationOver());
    }

    @Test
    void isSimulationOver_trueAtEnd() {
        simService.advanceTime(100);
        assertTrue(simService.isSimulationOver());
    }

    // ---- Buy stock ----

    @Test
    void buyStock_successfulPurchase() {
        User user = makeUser(1, new BigDecimal("10000.00"));
        when(stockDao.getStockPrice(1, "2010-01-04")).thenReturn(new BigDecimal("100.0000"));
        when(userDao.getUser(1)).thenReturn(user);

        String result = simService.buyStock(1, 1, 5);

        assertTrue(result.startsWith("Successfully"));
        verify(userDao).deductBalance(eq(1), any(BigDecimal.class));
        verify(userDao).addUserStock(1, 1, 5);
    }

    @Test
    void buyStock_insufficientFunds() {
        User user = makeUser(1, new BigDecimal("50.00"));
        when(stockDao.getStockPrice(1, "2010-01-04")).thenReturn(new BigDecimal("100.0000"));
        when(userDao.getUser(1)).thenReturn(user);

        String result = simService.buyStock(1, 1, 5);

        assertTrue(result.startsWith("Insufficient funds"));
        verify(userDao, never()).deductBalance(anyInt(), any());
        verify(userDao, never()).addUserStock(anyInt(), anyInt(), anyInt());
    }

    @Test
    void buyStock_noPriceData() {
        when(stockDao.getStockPrice(1, "2010-01-04")).thenReturn(null);

        String result = simService.buyStock(1, 1, 5);

        assertTrue(result.startsWith("No price data"));
    }

    // ---- Sell stock ----

    @Test
    void sellStock_successfulSale() {
        when(userDao.getOwnedShares(1, 1)).thenReturn(10);
        when(stockDao.getStockPrice(1, "2010-01-04")).thenReturn(new BigDecimal("150.0000"));

        String result = simService.sellStock(1, 1, 5);

        assertTrue(result.startsWith("Successfully"));
        verify(userDao).addBalance(eq(1), any(BigDecimal.class));
        verify(userDao).removeUserStock(1, 1, 5);
    }

    @Test
    void sellStock_insufficientShares() {
        when(userDao.getOwnedShares(1, 1)).thenReturn(2);

        String result = simService.sellStock(1, 1, 5);

        assertTrue(result.startsWith("Insufficient shares"));
        verify(userDao, never()).addBalance(anyInt(), any());
    }

    @Test
    void sellStock_noPriceData() {
        when(userDao.getOwnedShares(1, 1)).thenReturn(10);
        when(stockDao.getStockPrice(1, "2010-01-04")).thenReturn(null);

        String result = simService.sellStock(1, 1, 5);

        assertTrue(result.startsWith("No price data"));
    }

    // ---- Buy cost calculation ----

    @Test
    void buyStock_correctCostCalculation() {
        User user = makeUser(1, new BigDecimal("10000.00"));
        BigDecimal price = new BigDecimal("123.4500");
        when(stockDao.getStockPrice(1, "2010-01-04")).thenReturn(price);
        when(userDao.getUser(1)).thenReturn(user);

        simService.buyStock(1, 1, 3);

        // 123.4500 * 3 = 370.3500
        BigDecimal expectedCost = new BigDecimal("370.3500");
        verify(userDao).deductBalance(1, expectedCost);
    }

    // ---- Restart ----

    @Test
    void restartSimulation_resetsClockAndUser() {
        simService.advanceTime(3);
        assertEquals("2010-01-07", simService.getDate());

        BigDecimal balance = new BigDecimal("100000.00");
        simService.restartSimulation(1, balance);

        assertEquals("2010-01-04", simService.getDate());
        verify(userDao).resetUser(1, balance);
    }

    // ---- Delegation tests ----

    @Test
    void getOwnedStocks_delegatesToDao() {
        simService.getOwnedStocks(1);
        verify(stockDao).getOwnedStocks(1, "2010-01-04");
    }

    @Test
    void getStocksWithPriceChange_delegatesToDao() {
        simService.getStocksWithPriceChange();
        verify(stockDao).getStocksWithPriceChange("2010-01-04");
    }

    @Test
    void getPriceHistory_withDays_delegatesToDao() {
        simService.getPriceHistory(1, 30);
        verify(stockDao).getPriceHistory(1, "2010-01-04", 30);
    }

    // ---- Helper ----

    private User makeUser(int uid, BigDecimal balance) {
        User user = new User();
        user.setUid(uid);
        user.setUserName("testuser");
        user.setAccountBal(balance);
        return user;
    }
}
