/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

import mthree.stocksimulator.dao.StockDao;
import mthree.stocksimulator.dao.UserDao;

/**
 *
 * @author gabri
 */
public class SimServiceImpl implements SimService{
    private final StockDao stockDao;
    private final UserDao user;

    public SimServiceImpl(StockDao stockDao, UserDao user) {
        this.stockDao = stockDao;
        this.user = user;
    }
}
