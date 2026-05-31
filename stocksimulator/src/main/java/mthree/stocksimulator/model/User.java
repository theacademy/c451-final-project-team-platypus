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
    private final int uid;
    private final String userName;
    // Make sure the rounding mode is set correctly for this!
    private final BigDecimal accountBal;

    public User(int uid, String userName, BigDecimal accountBal) {
        this.uid = uid;
        this.userName = userName;
        this.accountBal = accountBal;
    }

    public int getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public BigDecimal getAccountBal() {
        return accountBal;
    }
    
    
}
