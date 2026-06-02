/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.util.List;
import mthree.stocksimulator.model.User;

/**
 *
 * @author gabri
 */
public interface UserService {
    
    /**
     * create new user
     * @param userName
     * @param startingBalance
     * @return 
     */
    public User createUser(String userName, BigDecimal startingBalance);
    
    public User getUser(int uid);
    
    public User getUserByUserName(String userName);
    
    public List<User> getAllUsers();
}
