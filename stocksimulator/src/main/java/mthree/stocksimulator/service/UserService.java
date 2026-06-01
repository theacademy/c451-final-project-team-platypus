/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mthree.stocksimulator.service;

import java.math.BigDecimal;
import java.util.List;
import mthree.stocksimulator.model.User;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 *
 * @author gabri
 */
public interface UserService {
    
    /**
     * creates new user with provided information and adds to database
     * @param userName
     * @param accountBal
     * @return 
     */
    public User createUser(String userName, BigDecimal accountBal);
    
    /**
     * Delete user from database
     * @param uid 
     */
    public void deleteUser(int uid);
    
    /**
     * Get user from DB by user id
     * @param uid
     * @return 
     * @throws EmptyResultDataAccessException if user not found
     */
    public User getUser(int uid) throws EmptyResultDataAccessException;
    
    /**
     * Gets all users from DB. Returns empty if no users exist
     * @return 
     */
    public List<User> getAllUsers();
    
    /**
     * Lookup user in database
     * @param userName
     * @return 
     * @throws EmptyResultDataAccessException if user not found
     */
    public User lookupUser(String userName) throws EmptyResultDataAccessException;
    
    /**
     * resets user balance to default. Unfortunately, the default balance is defined elsewhere so it is up to the caller to provide that default atm.
     * @param bal 
     */
    public void resetUserBalance(int bal);
}
