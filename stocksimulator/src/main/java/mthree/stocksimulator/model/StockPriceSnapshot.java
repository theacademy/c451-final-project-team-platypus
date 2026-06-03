/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.model;

import java.math.BigDecimal;

/**
 * A snapshot of the last 5 major price difference points for a given stock
 * @author gabri
 */
public class StockPriceSnapshot {
    private Stock stock;
    
    private BigDecimal currentPrice;
    private BigDecimal prevDayPrice;
    private BigDecimal prev7Price;
    private BigDecimal prev30Price;
    private BigDecimal prev1YPrice;

    public StockPriceSnapshot() {
    }

    public Stock getStock() {
        return stock;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public BigDecimal getPrevDayPrice() {
        return prevDayPrice;
    }

    public BigDecimal getPrev7Price() {
        return prev7Price;
    }

    public BigDecimal getPrev30Price() {
        return prev30Price;
    }

    public BigDecimal getPrev1YPrice() {
        return prev1YPrice;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setPrevDayPrice(BigDecimal prevDayPrice) {
        this.prevDayPrice = prevDayPrice;
    }

    public void setPrev7Price(BigDecimal prev7Price) {
        this.prev7Price = prev7Price;
    }

    public void setPrev30Price(BigDecimal prev30Price) {
        this.prev30Price = prev30Price;
    }

    public void setPrev1YPrice(BigDecimal prev1YPrice) {
        this.prev1YPrice = prev1YPrice;
    }
    
    
}
