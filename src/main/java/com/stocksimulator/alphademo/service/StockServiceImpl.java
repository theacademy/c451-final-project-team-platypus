package com.stocksimulator.alphademo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocksimulator.alphademo.dao.StockDaoImpl;
import com.stocksimulator.alphademo.dao.UserDaoImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.stocksimulator.alphademo.model.Stock;
import com.stocksimulator.alphademo.model.User;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;

@Service
public class StockServiceImpl implements StockService {

    private static final String API_KEY = "7BPW7D7FM9ECNP6M";
    private static final String BASE_URL = "https://www.alphavantage.co";
    private static final String START_DATE = "2000-01-01";
    private static final String END_DATE = "2020-01-01";

    private final StockDaoImpl stockDao;
    private final UserDaoImpl userDao;
    private final WebClient webClient;
    
    public int currentIndex = 0; // keeps track of current date
    public ArrayList<String> tradingDays; // stores all valid trading days
    
    

    public StockServiceImpl(StockDaoImpl stockDao, UserDaoImpl userDao, WebClient.Builder webClientBuilder) {
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

    public void advanceTime(int days) {
        currentIndex = Math.min(currentIndex + days, tradingDays.size() - 1);
    }
    
    
    public String getDate() {
        return tradingDays.get(currentIndex);
    }
    
    public Stock getStock(int sid) {
        try {
            return stockDao.getStock(sid, getDate());
        } catch (EmptyResultDataAccessException e) {
            System.out.println("No price data found for sid " + sid + " on " + getDate());
            return null;
        }
}
    
    public void fetchAndStoreSymbol(String symbol) {
        try {
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
                    rootNode = fallbackMapper.readTree(localJson);
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

        } catch (Exception e) {
            System.out.println("Network or parsing error encountered for: " + symbol);
            e.printStackTrace();
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

        // 5. Insert or update user_stocks — profitIfSold = current value of shares
        userDao.upsertUserStock(uid, sid, quantity, totalCost);

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

        // 4. Calculate proportional profitIfSold reduction
        // e.g. if user owns 10 shares and sells 4, reduce profitIfSold by 40%
        BigDecimal proportion = BigDecimal.valueOf(quantity)
                                          .divide(BigDecimal.valueOf(ownedShares), 4, RoundingMode.HALF_UP);
        BigDecimal currentProfit = stockDao.getProfitIfSold(uid, sid);
        BigDecimal profitReduction = currentProfit.multiply(proportion)
                                                  .setScale(4, RoundingMode.HALF_UP);

        // 5. Add sale value to user balance
        userDao.addBalance(uid, saleValue);

        // 6. Reduce shares and profitIfSold
        userDao.reduceUserStock(uid, sid, quantity, profitReduction);

        return "Successfully sold " + quantity + " share(s) of sid " + sid
             + " for " + saleValue + ". Profit/Loss on sold shares: "
             + saleValue.subtract(profitReduction);
    }
    
    public List<Stock> getStocksFromLast7Days() {
        return stockDao.getStocksFromLast7Days(getDate());
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
    
    public boolean isSimulationOver() {
        return currentIndex >= tradingDays.size() - 1;
}

}