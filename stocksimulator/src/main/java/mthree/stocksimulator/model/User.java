/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.model;

import java.math.BigDecimal;

/**
 *
 * @author gabri
 */
public class User {
    private int uid;
    private String userName;
    // Make sure the rounding mode is set correctly for this!
    private BigDecimal accountBal;

    public User() {}

    public int getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public BigDecimal getAccountBal() {
        return accountBal;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAccountBal(BigDecimal accountBal) {
        this.accountBal = accountBal;
    }
    
    
}
