/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.dao.StockDao;
import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;
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
    
    @Override
    public void advanceTime(int days) {
        currentIndex = Math.min(currentIndex + days, tradingDays.size() - 1);
    }
    
    @Override
    public String getDate() {
        return tradingDays.get(currentIndex);
    }
    
    @Override
    public Stock getStock(int sid) {
        return stockDao.getStock(sid);
    }
    
    @Override
    public List<StockPriceSnapshot> getStocksWithPriceChange() {
        return stockDao.getStocksWithPriceChange(getDate());
    }

    @Override
    public Map<String, BigDecimal> getPriceHistory(int sid) {
        return stockDao.getPriceHistory(sid, getDate());
    }

    @Override
    public Map<String, BigDecimal> getPriceHistory(int sid, int days) {
        return stockDao.getPriceHistory(sid, getDate(), days);
    }   
    
    @Override
    public boolean isSimulationOver() {
        return currentIndex >= tradingDays.size() - 1;
    }

    public void restartSimulation(int uid, BigDecimal startingBalance) {
        currentIndex = 0;
        userDao.resetUser(uid, startingBalance);
    }

    @Override
    public BigDecimal getStockPrice(int sid) throws EmptyResultDataAccessException {
        return stockDao.getStockPrice(sid);
    }

    @Override
    public Map<Integer, Integer> getAllOwnedStocks(int uid) {
        return stockDao.getAllOwnedStocks(uid);
    }
    
    @Override
    public int getOwnedStock(int uid, int sid){
        return stockDao.getOwnedStock(uid, sid);
    }


    @Override
    public BigDecimal buyStock(int uid, int sid, int quantity){
        stockDao.addUserStock(uid, sid, quantity);
        return stockDao.getStockPrice(sid).multiply(BigDecimal.valueOf(quantity)).negate(); //return money lost
    }

    @Override
    public BigDecimal sellStock(int uid, int sid, int quantity) throws InvalidOrderException{
        if (stockDao.getOwnedStock(uid, sid) < quantity) throw new InvalidOrderException("User does not have enough stocks in this company to sell!");
        stockDao.removeUserStock(uid, sid, quantity); //sell the stock
        return stockDao.getStockPrice(sid).multiply(BigDecimal.valueOf(quantity)); //return money made
    }

    @Override
    public void updateUserBal(int uid, BigDecimal quantity) throws InvalidOrderException{
        //check if quantity is negative (meaning we are deducting) then check if its bigger than the account bal
        if (quantity.compareTo(BigDecimal.ZERO) < 0 && userDao.getUser(uid).getAccountBal().compareTo(quantity.abs()) < 0) throw new InvalidOrderException("User does not have enough money to cover the transaction!");
        userDao.updateBalance(uid, quantity);
    }
}