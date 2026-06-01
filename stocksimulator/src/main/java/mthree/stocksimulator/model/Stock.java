/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.model;

/**
 *
 * @author gabri
 */
public class Stock {
    private int sid;
    private String stockName;
    private String stockCode;
    private java.math.BigDecimal stockPrice;

    public Stock() {}

    public int getSid() {
        return sid;
    }

    public String getStockName() {
        return stockName;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public java.math.BigDecimal getStockPrice() {
        return stockPrice;
    }

    public void setStockPrice(java.math.BigDecimal stockPrice) {
        this.stockPrice = stockPrice;
    }
}