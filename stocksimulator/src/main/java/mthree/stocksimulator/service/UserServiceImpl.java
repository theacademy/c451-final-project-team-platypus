/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import mthree.stocksimulator.dao.UserDao;
import mthree.stocksimulator.model.User;
import org.springframework.stereotype.Service;

/**
 *
 * @author gabri
 */
@Service
public class UserServiceImpl {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    // Create a new user with a set starting balance
    public User createUser(String userName, BigDecimal startingBalance) {
        User user = new User();
        BigDecimal balance = startingBalance.setScale(2, RoundingMode.HALF_UP);
        user.setUserName(userName);
        user.setAccountBal(balance);
        
        //add to db and set uid
        user = userDao.createUser(user);
        //System.out.println("Created user: " + userName + " with balance: " + balance);
        return user;
    }

    // Get a single user by uid
    public User getUser(int uid) {
        return userDao.getUser(uid);
    }

    // Look up a user by username (used for username-only login). Null if absent.
    public User getUserByUserName(String userName) {
        return userDao.getUserByUserName(userName);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }
}