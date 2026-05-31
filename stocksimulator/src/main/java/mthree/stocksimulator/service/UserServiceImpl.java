/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.User;

/**
 *
 * @author gabri
 */
public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    // Active user. Might not be needed.
    private User user;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }
    
    
}
