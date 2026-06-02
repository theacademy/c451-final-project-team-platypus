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
import org.springframework.dao.EmptyResultDataAccessException;

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
     * Gets current stock price for the given stock
     * @param sid
     * @return Stock price in BigDecimal format
     */
    public BigDecimal getStockPrice(int sid) throws EmptyResultDataAccessException;
    
    /**
     * Gets a mapping of all owned stocks by user
     * @param uid
     * @return owned stocks in a mapping of stockid to number of owned stock
     */
    public Map<Integer,Integer> getAllOwnedStocks(int uid);
    
    /**
     * Get number of owned stock by a user in a specific company
     * @param uid
     * @param sid
     * @return 
     */
    public int getOwnedStock(int uid, int sid);
    
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
     * first. Each row is ['yyyy-MM-dd', price].
     * @param sid
     * @param uptoDate inclusive upper bound
     * @return ordered map of {date, price} for stock
     */
    public Map<String, BigDecimal> getPriceHistory(int sid, String uptoDate);

    /**
     * Price history limited to the most recent N calendar days before uptoDate.
     * @param sid
     * @param uptoDate
     * @param days
     * @return 
     */
    public Map<String, BigDecimal> getPriceHistory(int sid, String uptoDate, int days);
}