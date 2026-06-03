/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author gabri
 */
public class StockPriceSnapshotMapper implements RowMapper<StockPriceSnapshot> {

    @Override
    public StockPriceSnapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
        
        StockPriceSnapshot sps = new StockPriceSnapshot();
        Stock stock = new Stock();
        stock.setSid(rs.getInt("sid"));
        stock.setStockName(rs.getString("stockName"));
        stock.setStockCode(rs.getString("stockCode"));
        // Stock, currentPrice, prevDayPrice, prev7Price, prev30Price, prev1YPrice
        
        sps.setStock(stock);
        sps.setCurrentPrice(rs.getBigDecimal("currentPrice"));
        sps.setPrevDayPrice(rs.getBigDecimal("prevDayPrice"));
        sps.setPrev7Price(rs.getBigDecimal("prev7Price"));
        sps.setPrev30Price(rs.getBigDecimal("prev30Price"));
        sps.setPrev1YPrice(rs.getBigDecimal("prev1YPrice"));
        
        return sps;
    }
    
}
