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

    // Deduct an amount from the user's balance
    @Override
    public void deductBalance(int uid, BigDecimal amount) {
        String sql = "UPDATE User SET accountBal = accountBal - ? WHERE uid = ?";
        jdbcTemplate.update(sql, amount, uid);
    }

    // Add an amount to the user's balance
    @Override
    public void addBalance(int uid, BigDecimal amount) {
        String sql = "UPDATE User SET accountBal = accountBal + ? WHERE uid = ?";
        jdbcTemplate.update(sql, amount, uid);
    }

    // Insert or update a user's stock position with new stock
    @Override
    public void addUserStock(int uid, int sid, int quantity) {
        String sql = """
                INSERT INTO user_stocks (User_uid, Stock_sid, ownedStock)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    ownedStock = ownedStock + VALUES(ownedStock),
                """;
        jdbcTemplate.update(sql, uid, sid, quantity);
    }

    // change owned shares from a user's position (used on sell)
    @Override
    public void removeUserStock(int uid, int sid, int quantity) {
        String sql = """
                UPDATE user_stocks
                SET ownedStock = ownedStock - ?,
                WHERE User_uid = ? AND Stock_sid = ?
                """;
        jdbcTemplate.update(sql, quantity, uid, sid);
    }

    // Get how many shares a user owns of a specific stock
    @Override
    public int getOwnedShares(int uid, int sid) {
        String sql = "SELECT ownedStock FROM user_stocks WHERE User_uid = ? AND Stock_sid = ?";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, uid, sid);
        return result != null ? result : 0;
    }
}
