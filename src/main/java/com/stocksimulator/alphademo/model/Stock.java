/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.stocksimulator.alphademo.model;

import java.math.BigDecimal;

/**
 *
 * @author jerem
 */
public class Stock {
    private final int sid;
    private final String stockName;
    private final String stockCode;
    //Make sure that you set the rounding mode to this correctly! It's DECIMAL(10,4) in the db
    private final BigDecimal stockPrice;

    public Stock(int sid, String stockName, String stockCode, BigDecimal stockPrice) {
        this.sid = sid;
        this.stockName = stockName;
        this.stockCode = stockCode;
        this.stockPrice = stockPrice;
    }

    public int getSid() {
        return sid;
    }

    public String getStockName() {
        return stockName;
    }

    public String getStockCode() {
        return stockCode;
    }

    public BigDecimal getStockPrice() {
        return stockPrice;
    }
}
