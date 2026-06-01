/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.util.List;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;

/**
 *
 * @author gabri
 */
public interface StockDao {
    
    public List<String> getAllTradingDays();
    
    public Stock getStock(int sid, String date);
    
    public List<Stock> getStocksFromLast7Days(String currentDate);
    
    public List<Stock> getOwnedStocks(int uid, String currentDate);
    
    public List<StockPriceSnapshot> getStocksWithPriceChange(String currentDate);
}
