/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.dao.StockDao;
import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;
import mthree.stocksimulator.model.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author gabri
 */
@Service
public class SimServiceImpl implements SimService {

    
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
        this.tradingDays = new ArrayList<>(stockDao.getAllTradingDays());
    }

    @Override
    public void advanceTime(int days) {
        currentIndex = Math.min(currentIndex + days, tradingDays.size() - 1);
    }
    
    
    @Override
    public String getDate() {
        return tradingDays.get(currentIndex);
    }
    
    @Override
    public Stock getStock(int sid) throws EmptyResultDataAccessException {
        return stockDao.getStock(sid);
    }   
    
    @Override
    public BigDecimal getStockPrice(int sid, String date) throws EmptyResultDataAccessException {
        return stockDao.getStockPrice(sid, date);
    }
    
    @Override
    public void buyStock(int uid, int sid, int quantity) {
        throw new UnsupportedOperationException("Implement me!");
        //TODO
    }
    
    @Override
    public void sellStock(int uid, int sid, int quantity) {
        throw new UnsupportedOperationException("Implement me!");
        //TODO
    }
    
    @Override
    public Map<Integer, Integer> getOwnedStocks(int uid) {
        return stockDao.getOwnedStocks(uid);
    }

    @Override
    public int getOwnedShares(int uid, int sid) {
        return userDao.getOwnedShares(uid, sid);
    }
    
    @Override
    public List<StockPriceSnapshot> getStocksWithPriceChange() {
        return stockDao.getStocksWithPriceChange(getDate());
    }   
    
    @Override
    public boolean isSimulationOver() {
        return currentIndex >= tradingDays.size() - 1;
    }

    @Override
    public void updateUserBal(int uid, int quantity) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
