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
    
    /**
     * update stock_history with new information of stock bought/sold. If quantity is negative, assume its sold
     * @param uid
     * @param sid
     * @param quantity 
     */
    public void updateUserStock(int uid, int sid, int quantity);

    /**
     * Price history for one stock up to and including the given date, oldest
     * first. Each row is [date 'yyyy-MM-dd', price].
     * @param sid
     * @param uptoDate inclusive upper bound
     * @return ordered list of {date, price} maps
     */
    public List<Map<String, BigDecimal>> getPriceHistory(int sid, String uptoDate);

    /**
     * Price history limited to the most recent N calendar days before uptoDate.
     * @param sid
     * @param uptoDate
     * @param days
     * @return 
     */
    public List<Map<String, BigDecimal>> getPriceHistory(int sid, String uptoDate, int days);
}