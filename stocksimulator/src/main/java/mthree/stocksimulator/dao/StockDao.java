/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;

/**
 *
 * @author gabri
 */
public interface StockDao {
    
    public List<String> getAllTradingDays();
    
    /**
     * get a stock given its ID
     * @param sid
     * @return Stock in Stock object format
     */
    public Stock getStock(int sid);
    
    /**
     * Gets stock price for the given stock and the given date
     * @param sid
     * @param currentDate
     * @return Stock price in BigDecimal format
     */
    public BigDecimal getStockPrice(int sid, String currentDate);
    
    /**
     * Gets a mapping of all owned stocks paired with the stock id
     * @param uid
     * @return Map of stock id and stock owned in said stock id
     */
    public Map<Integer, Integer> getOwnedStocks(int uid);
    
    /**
     * Obtain price changes for ALL stocks in last day, week, month, and year
     * @param currentDate
     * @return List of StockPriceSnapshots containing needed data
     */
    public List<StockPriceSnapshot> getStocksWithPriceChange(String currentDate);
}
