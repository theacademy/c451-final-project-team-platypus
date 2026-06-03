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

        return stock;
    }
}