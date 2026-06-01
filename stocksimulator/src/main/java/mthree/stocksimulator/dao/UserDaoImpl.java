/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
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
public class UserDaoImpl {

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Create a new user with a starting balance
    public User createUser(String userName, BigDecimal startingBalance) {
        String sql = "INSERT INTO User (userName, accountBal) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, userName);
            ps.setBigDecimal(2, startingBalance);
            return ps;
        }, keyHolder);

        int newUid = keyHolder.getKey().intValue();
        return new User(newUid, userName, startingBalance);
    }

    // Get a user by their uid
    public User getUser(int uid) {
        String sql = "SELECT uid, userName, accountBal FROM User WHERE uid = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new User(
                rs.getInt("uid"),
                rs.getString("userName"),
                rs.getBigDecimal("accountBal")
        ), uid);
    }

    // Get all users
    public List<User> getAllUsers() {
        String sql = "SELECT uid, userName, accountBal FROM User";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new User(
                rs.getInt("uid"),
                rs.getString("userName"),
                rs.getBigDecimal("accountBal")
        ));
    }

    // Deduct an amount from the user's balance
    public void deductBalance(int uid, BigDecimal amount) {
        String sql = "UPDATE User SET accountBal = accountBal - ? WHERE uid = ?";
        jdbcTemplate.update(sql, amount, uid);
    }

    // Add an amount to the user's balance
    public void addBalance(int uid, BigDecimal amount) {
        String sql = "UPDATE User SET accountBal = accountBal + ? WHERE uid = ?";
        jdbcTemplate.update(sql, amount, uid);
    }

    // Insert or update a user's stock position
    // profitIfSold accumulates as more shares are bought
    public void upsertUserStock(int uid, int sid, int quantity, BigDecimal profitIfSold) {
        String sql = """
                INSERT INTO user_stocks (User_uid, Stock_sid, ownedStock, profitIfSold)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    ownedStock = ownedStock + VALUES(ownedStock),
                    profitIfSold = profitIfSold + VALUES(profitIfSold)
                """;
        jdbcTemplate.update(sql, uid, sid, quantity, profitIfSold);
    }

    // Remove shares from a user's position (used on sell)
    public void reduceUserStock(int uid, int sid, int quantity, BigDecimal profitReduction) {
        String sql = """
                UPDATE user_stocks
                SET ownedStock = ownedStock - ?,
                    profitIfSold = profitIfSold - ?
                WHERE User_uid = ? AND Stock_sid = ?
                """;
        jdbcTemplate.update(sql, quantity, profitReduction, uid, sid);
    }

    // Get how many shares a user owns of a specific stock
    public int getOwnedShares(int uid, int sid) {
        String sql = "SELECT ownedStock FROM user_stocks WHERE User_uid = ? AND Stock_sid = ?";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, uid, sid);
        return result != null ? result : 0;
    }
}
