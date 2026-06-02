/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.dao.StockDao;
import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.Stock;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * @author gabri
 */
@Service
public class SimServiceImpl implements SimService {

    private final StockDao stockDao;
    private final UserDao userDao;
    
    public int currentIndex = 0; // keeps track of current date
    public ArrayList<String> tradingDays; // stores all valid trading days
    
    

    public SimServiceImpl(StockDao stockDao, UserDao userDao, WebClient.Builder webClientBuilder) {
        this.stockDao = stockDao;
        this.userDao = userDao;
            if (tradingDays == null) {
                loadTradingDays();
        }
    }
    private void loadTradingDays() {
        tradingDays = new ArrayList<>(stockDao.getAllTradingDays());
}

    // In web mode no CommandLineRunner pre-loads the days, so load on first use.
    private void ensureTradingDaysLoaded() {

    }

    @Override
    public void advanceTime(int days) {
        ensureTradingDaysLoaded();
        currentIndex = Math.min(currentIndex + days, tradingDays.size() - 1);
    }
    
    
    @Override
    public String getDate() {
        ensureTradingDaysLoaded();
        return tradingDays.get(currentIndex);
    }
    
    @Override
    public Stock getStock(int sid) {
        try {
            return stockDao.getStock(sid);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("No price data found for sid " + sid + " on " + getDate());
            return null;
        }
    }

    @Override
    public int getOwnedShares(int uid, int sid) {
        return userDao.getOwnedShares(uid, sid);
    }
    
    public List<Stock[]> getStocksWithPriceChange() {
        return stockDao.getStocksWithPriceChange(getDate());
    }

    public List<Map<String, Object>> getPriceHistory(int sid) {
        return stockDao.getPriceHistory(sid, getDate());
    }

    public List<Map<String, Object>> getPriceHistory(int sid, int days) {
        return stockDao.getPriceHistory(sid, getDate(), days);
    }   
    
    @Override
    public boolean isSimulationOver() {
        ensureTradingDaysLoaded();
        return currentIndex >= tradingDays.size() - 1;
    }

    public void restartSimulation(int uid, java.math.BigDecimal startingBalance) {
        currentIndex = 0;
        userDao.resetUser(uid, startingBalance);
    }
}