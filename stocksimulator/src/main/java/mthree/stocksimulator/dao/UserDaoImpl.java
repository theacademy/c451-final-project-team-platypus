/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import mthree.stocksimulator.dao.mappers.UserMapper;
import mthree.stocksimulator.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 *
 * @author gabri
 */
@Repository
public class UserDaoImpl implements UserDao {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Create a new user with a starting balance
    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO User (userName, accountBal) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUserName());
            ps.setBigDecimal(2, user.getAccountBal());
            return ps;
        }, keyHolder);

        user.setUid(keyHolder.getKey().intValue());
        return user;
    }

    // Get a user by their uid
    @Override
    public User getUser(int uid) {
        String sql = "SELECT uid, userName, accountBal FROM User WHERE uid = ?";
        return jdbcTemplate.queryForObject(sql, new UserMapper(), uid);
    }

    // Get a user by their username (null if not found)
    @Override
    public User getUserByUserName(String userName) {
        String sql = "SELECT uid, userName, accountBal FROM User WHERE userName = ?";
        List<User> users = jdbcTemplate.query(sql, new UserMapper(), userName);
        return users.isEmpty() ? null : users.get(0);
    }
    
    // Update user name
    @Override
    public void updateUserName(int uid, String newName){
        String sql = "UPDATE User SET userName = ? WHERE uid = ?";
        jdbcTemplate.update(sql, newName, uid);
    }

    // Get all users
    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT uid, userName, accountBal FROM User";
        return jdbcTemplate.query(sql, new UserMapper());
    }

    // Update user's balance
    @Override
    public void updateBalance(int uid, BigDecimal amount) {
        String sql = "UPDATE User SET accountBal = accountBal + ? WHERE uid = ?";
        jdbcTemplate.update(sql, amount, uid);
    }

    // Reset a user's portfolio: clear all stocks, set balance
    @Override 
    public void resetUser(int uid, BigDecimal balance) {
        jdbcTemplate.update("DELETE FROM user_stocks WHERE User_uid = ?", uid);
        jdbcTemplate.update("UPDATE User SET accountBal = ? WHERE uid = ?", balance, uid);
    }
}