/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.util.List;
import mthree.stocksimulator.model.User;

/**
 *
 * @author gabri
 */
public interface UserDao {
    
    public User createUser(User user);
    
    public User getUser(int uid);
    
    public List<User> getAllUsers();
    
    public void updateUserName(int uid, String newName);
    
    public void deductBalance(int uid, BigDecimal amount);
    
    public void addBalance(int uid, BigDecimal amount);
    
    public void addUserStock(int uid, int sid, int quantity);
    
    public void removeUserStock(int uid, int sid, int quantity);
   
    public int getOwnedShares(int uid, int sid);
}
