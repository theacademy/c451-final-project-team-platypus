/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.util.List;
import mthree.stocksimulator.model.Stock;

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
     * Gets a list of the stocks a user owns, each carrying its price on the
     * given date.
     * @param uid
     * @param currentDate
     * @return List of owned Stocks (with stockPrice populated for currentDate)
     */
    public List<Stock> getOwnedStocks(int uid, String currentDate);

    /**
     * Obtain price changes for ALL stocks in last day, week, month, and year
     * @param currentDate
     * @return List of Stock[] arrays: [current, prevDay, prev7, prev30, prevYear]
     */
    public List<Stock[]> getStocksWithPriceChange(String currentDate);

    /**
     * Look up a Stock's sid by its code, creating the Stock row if absent.
     * @param symbol the stock ticker/code
     * @return the sid of the existing or newly created Stock
     */
    public int getOrCreateStockId(String symbol);

    /**
     * Price history for one stock up to and including the given date, oldest
     * first. Each row is [date 'yyyy-MM-dd', price].
     * @param sid
     * @param uptoDate inclusive upper bound
     * @return ordered list of {date, price} maps
     */
    public List<java.util.Map<String, Object>> getPriceHistory(int sid, String uptoDate);

    /**
     * Batch-insert rows into Stock_history.
     * @param historyRows rows of [Stock_sid, date, stockPrice]
     */
    public void insertAllStockHistory(List<Object[]> historyRows);
}