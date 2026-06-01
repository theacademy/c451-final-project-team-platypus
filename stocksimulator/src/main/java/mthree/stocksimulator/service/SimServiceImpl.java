/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import mthree.stocksimulator.dao.StockDao;
import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author gabri
 */
@Service
public class SimServiceImpl implements SimService {

    @Value("${api.key}")
    private String API_KEY;
    
    private static final String BASE_URL = "https://www.alphavantage.co";
    private static final String START_DATE = "2000-01-01";
    private static final String END_DATE = "2020-01-01";

    private final StockDao stockDao;
    private final UserDao userDao;
    private final WebClient webClient;
    
    public int currentIndex = 0; // keeps track of current date
    public ArrayList<String> tradingDays; // stores all valid trading days
    
    

    public SimServiceImpl(StockDao stockDao, UserDao userDao, WebClient.Builder webClientBuilder) {
        this.stockDao = stockDao;
        this.userDao = userDao;
        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(32 * 1024 * 1024)) // 32MB buffer
                .build();
    }
    public void loadTradingDays() {
        tradingDays = new ArrayList<>(stockDao.getAllTradingDays());
}

    // In web mode no CommandLineRunner pre-loads the days, so load on first use.
    private void ensureTradingDaysLoaded() {
        if (tradingDays == null) {
            loadTradingDays();
        }
    }

    public void advanceTime(int days) {
        ensureTradingDaysLoaded();
        currentIndex = Math.min(currentIndex + days, tradingDays.size() - 1);
    }
    
    
    public String getDate() {
        ensureTradingDaysLoaded();
        return tradingDays.get(currentIndex);
    }
    
    public Stock getStock(int sid) {
        try {
            return stockDao.getStock(sid);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("No price data found for sid " + sid + " on " + getDate());
            return null;
        }
}
    
    public void fetchAndStoreSymbol(String symbol) {
            System.out.println("Fetching data for " + symbol + " via WebClient...");

            // 1. Fetch and parse via WebClient
            JsonNode rootNode = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "TIME_SERIES_DAILY")
                            .queryParam("symbol", symbol)
                            .queryParam("outputsize", "full")
                            .queryParam("apikey", API_KEY)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            // 2. Rate limit check & local file fallback
            if (rootNode == null || !rootNode.has("Time Series (Daily)")) {
                System.out.println("API rate limit exceeded for " + symbol + ".");
                System.out.println("Pivoting data strategy: Attempting to load from local JSON file...");

                File localJson = new File("mock_" + symbol.toLowerCase() + "_data.json");
                if (localJson.exists()) {
                    ObjectMapper fallbackMapper = new ObjectMapper();
                    try {
                        rootNode = fallbackMapper.readTree(localJson);
                    } catch (java.io.IOException e) {
                        System.out.println("Failed to read local file for " + symbol + ": " + e.getMessage());
                        return;
                    }
                } else {
                    System.out.println("Local file not found. Aborting data fetch for " + symbol);
                    return;
                }
            }

            // 3. Resolve the Stock's sid — creates the Stock row if it doesn't exist yet
            int stockSid = stockDao.getOrCreateStockId(symbol);

            // 4. Extract and filter the time series data
            JsonNode timeSeriesNode = rootNode.get("Time Series (Daily)");
            List<Object[]> historyRows = new ArrayList<>();

            timeSeriesNode.fields().forEachRemaining(entry -> {
                String date = entry.getKey();
                if (date.compareTo(START_DATE) >= 0 && date.compareTo(END_DATE) <= 0) {
                    BigDecimal price = new BigDecimal(entry.getValue().get("1. open").asText())
                            .setScale(4, RoundingMode.HALF_UP);
                    // Store as [stockSid, date, price] for batch insert
                    historyRows.add(new Object[]{stockSid, LocalDateTime.parse(date + "T00:00:00"), price});
                }
            });

            // 5. Batch insert into Stock_history
            if (!historyRows.isEmpty()) {
                stockDao.insertAllStockHistory(historyRows);
                System.out.println("-> Successfully inserted " + historyRows.size() + " history rows for " + symbol);
            } else {
                System.out.println("-> No valid dates found in the specified range for " + symbol);
            }
    }
    
    public String buyStock(int uid, int sid, int quantity) {
        // 1. Get current price from simulator date
        BigDecimal stockPrice = stockDao.getStockPrice(sid, getDate());
        if (stockPrice == null) {
            return "No price data available for this stock on " + getDate();
        }

        // 2. Calculate total cost
        BigDecimal totalCost = stockPrice.multiply(BigDecimal.valueOf(quantity))
                                         .setScale(4, RoundingMode.HALF_UP);

        // 3. Check user can afford it
        User user = userDao.getUser(uid);
        if (user.getAccountBal().compareTo(totalCost) < 0) {
            return "Insufficient funds. Cost: " + totalCost + ", Balance: " + user.getAccountBal();
        }

        // 4. Deduct balance
        userDao.deductBalance(uid, totalCost);

        // 5. Insert or update the user's position in user_stocks
        userDao.addUserStock(uid, sid, quantity);

        return "Successfully purchased " + quantity + " share(s) of sid " + sid
             + " for " + totalCost + ". Remaining balance: " + user.getAccountBal().subtract(totalCost);
    }
    
    public String sellStock(int uid, int sid, int quantity) {
        // 1. Check user owns enough shares
        int ownedShares = userDao.getOwnedShares(uid, sid);
        if (ownedShares < quantity) {
            return "Insufficient shares. Owned: " + ownedShares + ", Requested to sell: " + quantity;
        }

        // 2. Get current price from simulator date
        BigDecimal stockPrice = stockDao.getStockPrice(sid, getDate());
        if (stockPrice == null) {
            return "No price data available for this stock on " + getDate();
        }

        // 3. Calculate sale value
        BigDecimal saleValue = stockPrice.multiply(BigDecimal.valueOf(quantity))
                                         .setScale(4, RoundingMode.HALF_UP);

        // 4. Add sale value to user balance
        userDao.addBalance(uid, saleValue);

        // 5. Reduce owned shares
        userDao.removeUserStock(uid, sid, quantity);

        return "Successfully sold " + quantity + " share(s) of sid " + sid
             + " for " + saleValue + ".";
    }
    
    public List<Stock> getOwnedStocks(int uid) {
        return stockDao.getOwnedStocks(uid, getDate());
    }

    public int getOwnedShares(int uid, int sid) {
        return userDao.getOwnedShares(uid, sid);
    }
    
    public List<Stock[]> getStocksWithPriceChange() {
        return stockDao.getStocksWithPriceChange(getDate());
    }

    public List<java.util.Map<String, Object>> getPriceHistory(int sid) {
        return stockDao.getPriceHistory(sid, getDate());
    }   
    
    public boolean isSimulationOver() {
        ensureTradingDaysLoaded();
        return currentIndex >= tradingDays.size() - 1;
    }
}