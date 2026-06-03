/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.dao.mappers.StockMapper;
import mthree.stocksimulator.dao.mappers.StockPriceSnapshotMapper;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 *
 * @author gabri
 */
@Repository
public class StockDaoImpl implements StockDao{
    
    private final JdbcTemplate jdbcTemplate;

    public StockDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<String> getAllTradingDays() {
        String sql = "SELECT DISTINCT DATE_FORMAT(date, '%Y-%m-%d') AS tradingDay FROM Stock_history ORDER BY tradingDay ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public Stock getStock(int sid) {
        String sql = "SELECT * FROM Stock WHERE sid = ?";

        return jdbcTemplate.queryForObject(sql, new StockMapper(), sid);
    }
    
    @Override
    public BigDecimal getStockPrice(int sid) throws EmptyResultDataAccessException{
        String sql = "SELECT stockPrice FROM Stock_history WHERE Stock_sid = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, sid);
    }
    
    @Override
    public Map<String, BigDecimal> getPriceHistory(int sid, String uptoDate) {
        String sql = """
                SELECT DATE_FORMAT(date, '%Y-%m-%d') AS date, stockPrice AS price
                FROM Stock_history
                WHERE Stock_sid = ? AND DATE(date) <= ?
                ORDER BY date ASC
                """;
        return jdbcTemplate.query(sql, rs -> {
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        while (rs.next()) {
            result.put(
                rs.getString("date"),
                rs.getBigDecimal("price")
            );
        }
        return result;
        }, sid, uptoDate); 
    }

    @Override
    public Map<String, BigDecimal> getPriceHistory(int sid, String uptoDate, int days) {
        String sql = """
                SELECT DATE_FORMAT(date, '%Y-%m-%d') AS date, stockPrice AS price
                FROM Stock_history
                WHERE Stock_sid = ? AND DATE(date) <= ? AND DATE(date) >= DATE_SUB(?, INTERVAL ? DAY)
                ORDER BY date ASC
                """;
        return jdbcTemplate.query(sql, rs -> {
            Map<String, BigDecimal> result = new LinkedHashMap<>();

            while (rs.next()) {
                result.put(
                    rs.getString("date"),
                    rs.getBigDecimal("price")
                );
            }
            return result;
        }, sid, uptoDate, uptoDate, days);
    }
    
    @Override
    public Map<Integer, Integer> getAllOwnedStocks(int uid) {
        String sql = "SELECT (sid, ownedStock) FROM stock_history WHERE uid = ?";

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, Integer> result = new LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getInt("sid"), rs.getInt("ownedStock"));
            }
            return result;
        }, uid);
       
    }
    
    @Override
    public int getOwnedStock(int uid, int sid) {
        String sql = "SELECT COALESCE(SUM(ownedStock), 0) FROM user_stocks WHERE User_uid = ? AND Stock_sid = ?";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, uid, sid);
        return result != null ? result : 0;
    }

    @Override
    public List<StockPriceSnapshot> getStocksWithPriceChange(String currentDate) {
            // Instead of 5 correlated subqueries per stock (which re-scans Stock_history
            // once per stock per period), we pre-compute one anchor date per period across
            // ALL stocks in a single pass using MAX(date) GROUP BY Stock_sid.
            // Each period then becomes a plain equality JOIN — no subquery fan-out.
            String sql = """
                    WITH anchors AS (
                        SELECT
                            Stock_sid,
                            MAX(CASE WHEN DATE(date) = ?                        THEN DATE(date) END) AS d_curr,
                            MAX(CASE WHEN DATE(date) < ?                        THEN DATE(date) END) AS d_prev1,
                            MAX(CASE WHEN DATE(date) <= DATE_SUB(?, INTERVAL 7 DAY)  THEN DATE(date) END) AS d_prev7,
                            MAX(CASE WHEN DATE(date) <= DATE_SUB(?, INTERVAL 30 DAY) THEN DATE(date) END) AS d_prev30,
                            MAX(CASE WHEN DATE(date) <= DATE_SUB(?, INTERVAL 1 YEAR) THEN DATE(date) END) AS d_prev1Y
                        FROM Stock_history
                        GROUP BY Stock_sid
                    )
                    SELECT
                        s.sid, s.stockName, s.stockCode,
                        curr.stockPrice  AS currentPrice,
                        p1.stockPrice    AS prevDayPrice,
                        p7.stockPrice    AS prev7Price,
                        p30.stockPrice   AS prev30Price,
                        p1Y.stockPrice   AS prev1YPrice
                    FROM Stock s
                    JOIN anchors       a   ON s.sid = a.Stock_sid
                    JOIN Stock_history curr ON curr.Stock_sid = a.Stock_sid AND DATE(curr.date) = a.d_curr
                    LEFT JOIN Stock_history p1  ON p1.Stock_sid  = a.Stock_sid AND DATE(p1.date)  = a.d_prev1
                    LEFT JOIN Stock_history p7  ON p7.Stock_sid  = a.Stock_sid AND DATE(p7.date)  = a.d_prev7
                    LEFT JOIN Stock_history p30 ON p30.Stock_sid = a.Stock_sid AND DATE(p30.date) = a.d_prev30
                    LEFT JOIN Stock_history p1Y ON p1Y.Stock_sid = a.Stock_sid AND DATE(p1Y.date) = a.d_prev1Y
                    ORDER BY s.stockCode
                    """;

            return jdbcTemplate.query(sql, new StockPriceSnapshotMapper() , currentDate, currentDate, currentDate, currentDate, currentDate);
        }
   
    // Insert or update a user's stock position with new stock
    @Override
    public void addUserStock(int uid, int sid, int quantity) {
        String sql = """
                INSERT INTO user_stocks (User_uid, Stock_sid, ownedStock)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    ownedStock = ownedStock + VALUES(ownedStock)
                """;
        jdbcTemplate.update(sql, uid, sid, quantity);
    }

    // change owned shares from a user's position (used on sell)
    @Override
    public void removeUserStock(int uid, int sid, int quantity) {
        String sql = """
                UPDATE user_stocks
                SET ownedStock = ownedStock - ?
                WHERE User_uid = ? AND Stock_sid = ?
                """;
        jdbcTemplate.update(sql, quantity, uid, sid);
    }
    
}