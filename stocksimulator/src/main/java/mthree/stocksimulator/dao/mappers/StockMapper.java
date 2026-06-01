/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;
import mthree.stocksimulator.model.Stock;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author gabri
 */
public class StockMapper implements RowMapper<Stock> {
    @Override
    public Stock mapRow(ResultSet rs, int rowNum) throws SQLException {

        //sid, stockName, stockCode
        Stock stock = new Stock();
        stock.setSid(rs.getInt("sid"));
        stock.setStockName(rs.getString("stockName"));
        stock.setStockCode(rs.getString("stockCode"));

        // stockPrice is only present when the query joins Stock_history
        if (hasColumn(rs, "stockPrice")) {
            stock.setStockPrice(rs.getBigDecimal("stockPrice"));
        }

        return stock;
    }

    private boolean hasColumn(ResultSet rs, String column) throws SQLException {
        java.sql.ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            if (column.equalsIgnoreCase(meta.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }  
}