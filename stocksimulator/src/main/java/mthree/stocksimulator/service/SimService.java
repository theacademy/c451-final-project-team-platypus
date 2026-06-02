/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;
import org.springframework.dao.EmptyResultDataAccessException;

public interface SimService {
    /**
     * gets current trading date
     * @return date as String
     */
    public String getDate();
    
    /**
     * Advances trading day by given number of trading days
     * @param days as trading days. NOT normal days
     */
    public void advanceTime(int days);
    
    /**
     * get the stock object given the stock ID
     * @param sid
     * @return
     * @throws EmptyResultDataAccessException if no stock was found for the given stock ID
     */
    public Stock getStock(int sid) throws EmptyResultDataAccessException;
    
    /**
     * get the price for the given stock and the given date
     * @param sid
     * @param date must be a valid trading day
     * @return
     * @throws EmptyResultDataAccessException if no price was found for the stock, usually because the date is wrong
     */
    public BigDecimal getStockPrice(int sid, String date) throws EmptyResultDataAccessException;
        
    /**
     * Gets the list of owned stock for the given user in a mapping of stock ids to number of stock owned for that company
     * @param uid
     * @return map of sid to owned stocks in that sid
     */
    public Map<Integer,Integer> getOwnedStocks(int uid);
    
    /**
     * Get price history of a given stock as a map
     * @param sid
     * @return 
     */
    public Map<String, BigDecimal> getPriceHistory(int sid);

    /**
     * get price history of a given stock as far back as the provided days
     * @param sid
     * @param days
     * @return 
     */
    public Map<String, BigDecimal> getPriceHistory(int sid, int days);
    
    /**
     * Gets the amount of stock owned in a specific company by the specific user
     * @param uid
     * @param sid
     * @return 
     */
    public int getOwnedShares(int uid, int sid);
    
    /**
     * Buys for the user stock in the given company. 
     * Does not update user balance
     * @param uid
     * @param sid
     * @param quantity 
     */
    public void buyStock(int uid, int sid, int quantity) throws InvalidOrderException;
    
    /**
     * Sells the given amount of stock for the given company for the given user.
     * Does not update user balance
     * @param uid
     * @param sid
     * @param quantity
     */
    public void sellStock(int uid, int sid, int quantity) throws InvalidOrderException;
    
    /**
     * add/remove dollars from user account
     * @param uid
     * @param quantity 
     */
    public void updateUserBal(int uid, BigDecimal quantity) throws InvalidOrderException;
    
    /**
     * Gets the stock price change information for all stocks.
     * @return List of StockPriceSnapshot DTOs containing the stock and major price point differences in past
     */
    public List<StockPriceSnapshot> getStocksWithPriceChange();
    
    /**
     * Tells caller if the simulation is over yet or not based of it it reached the end of trading days
     * @return true if over. False if there are still trading days to do
     */
    public boolean isSimulationOver();
}
