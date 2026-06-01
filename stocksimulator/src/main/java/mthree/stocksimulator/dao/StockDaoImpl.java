/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import mthree.stocksimulator.model.Stock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
    
    /**
 * Checks if a stock exists by its symbol.
 * If it exists, returns the existing sid.
 * If not, inserts it and returns the newly generated sid.
 */
    public int getOrCreateStockId(String symbol) {
        // Check if stock already exists
        String selectSql = "SELECT sid FROM Stock WHERE stockCode = ?";
        List<Integer> ids = jdbcTemplate.queryForList(selectSql, Integer.class, symbol);

        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        // Insert new stock row with just the code — name defaults to code until updated
        String insertSql = "INSERT INTO Stock (stockName, stockCode) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, symbol); // stockName = symbol for now
            ps.setString(2, symbol); // stockCode = symbol
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }
    
    public List<String> getAllTradingDays() {
        String sql = "SELECT DISTINCT DATE_FORMAT(date, '%Y-%m-%d') AS tradingDay FROM Stock_history ORDER BY tradingDay ASC";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public Stock getStock(int sid, String date) {
    String sql = """
            SELECT s.sid, s.stockName, s.stockCode, sh.stockPrice
            FROM Stock s
            JOIN Stock_history sh ON s.sid = sh.Stock_sid
            WHERE s.sid = ? AND DATE(sh.date) = ?
            """;

    return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Stock(
            rs.getInt("sid"),
            rs.getString("stockName"),
            rs.getString("stockCode"),
            rs.getBigDecimal("stockPrice")
    ), sid, date);
}
    
    public void insertAllStockHistory(List<Object[]> historyRows) {
        String sql = "INSERT INTO Stock_history (Stock_sid, date, stockPrice) VALUES (?, ?, ?)";
        int batchSize = 1000;

        jdbcTemplate.batchUpdate(
            sql,
            historyRows,
            batchSize,
            (PreparedStatement ps, Object[] row) -> {
                ps.setInt(1, (int) row[0]);
                ps.setObject(2, row[1]);  // LocalDateTime maps to DATETIME
                ps.setBigDecimal(3, (BigDecimal) row[2]);
            }
        );
}

    public void insertAllStocks(List<Stock> stocks) {
        // The SQL statement
        String sql = "INSERT INTO Stock(symbol, date, open, close) VALUES(?, ?, ?, ?)";
        
        // Chunk size: 500 to 1000 is generally the sweet spot for databases
        int batchSize = 1000; 

        // Spring's chunking batchUpdate method
        jdbcTemplate.batchUpdate(
            sql,
            stocks,
            batchSize,
            (PreparedStatement ps, Stock stock) -> {
                ps.setInt(1, stock.getSid());
                ps.setString(2, stock.getStockName());
                ps.setString(3, stock.getStockCode());
                ps.setBigDecimal(4, stock.getStockPrice());
            }
        );
    }
    public BigDecimal getStockPrice(int sid, String date) {
        String sql = "SELECT stockPrice FROM Stock_history WHERE Stock_sid = ? AND DATE(date) = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, sid, date);
    }   
    
    public BigDecimal getProfitIfSold(int uid, int sid) {
        String sql = "SELECT profitIfSold FROM user_stocks WHERE User_uid = ? AND Stock_sid = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, uid, sid);
    }
    
    public List<Stock> getStocksFromLast7Days(String currentDate) {
        String sql = """
                SELECT s.sid, s.stockName, s.stockCode, sh.stockPrice
                FROM Stock s
                JOIN Stock_history sh ON s.sid = sh.Stock_sid
                WHERE sh.date <= ? AND sh.date >= DATE_SUB(?, INTERVAL 7 DAY)
                ORDER BY s.stockCode, sh.date DESC
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Stock(
                rs.getInt("sid"),
                rs.getString("stockName"),
                rs.getString("stockCode"),
                rs.getBigDecimal("stockPrice")
        ), currentDate, currentDate);
    }
    
    public List<Stock> getOwnedStocks(int uid, String currentDate) {
        String sql = """
                SELECT s.sid, s.stockName, s.stockCode, sh.stockPrice
                FROM Stock s
                JOIN user_stocks us ON s.sid = us.Stock_sid
                JOIN Stock_history sh ON s.sid = sh.Stock_sid
                WHERE us.User_uid = ? AND DATE(sh.date) = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Stock(
                rs.getInt("sid"),
                rs.getString("stockName"),
                rs.getString("stockCode"),
                rs.getBigDecimal("stockPrice")
        ), uid, currentDate);
    }

    public List<Stock[]> getStocksWithPriceChange(String currentDate) {
        String sql = """
                SELECT
                    s.sid, s.stockName, s.stockCode,
                    curr.stockPrice AS currentPrice,
                    prev1.stockPrice AS prevDayPrice,
                    prev7.stockPrice AS prev7Price,
                    prev30.stockPrice AS prev30Price,
                    prev1Y.stockPrice AS prev1YPrice
                FROM Stock s
                JOIN Stock_history curr
                    ON s.sid = curr.Stock_sid AND DATE(curr.date) = ?
                LEFT JOIN Stock_history prev1
                    ON s.sid = prev1.Stock_sid AND DATE(prev1.date) = (
                        SELECT DATE(date) FROM Stock_history
                        WHERE Stock_sid = s.sid AND DATE(date) < ?
                        ORDER BY date DESC LIMIT 1
                    )
                LEFT JOIN Stock_history prev7
                    ON s.sid = prev7.Stock_sid AND DATE(prev7.date) = (
                        SELECT DATE(date) FROM Stock_history
                        WHERE Stock_sid = s.sid AND DATE(date) <= DATE_SUB(?, INTERVAL 7 DAY)
                        ORDER BY date DESC LIMIT 1
                    )
                LEFT JOIN Stock_history prev30
                    ON s.sid = prev30.Stock_sid AND DATE(prev30.date) = (
                        SELECT DATE(date) FROM Stock_history
                        WHERE Stock_sid = s.sid AND DATE(date) <= DATE_SUB(?, INTERVAL 30 DAY)
                        ORDER BY date DESC LIMIT 1
                    )
                LEFT JOIN Stock_history prev1Y
                    ON s.sid = prev1Y.Stock_sid AND DATE(prev1Y.date) = (
                        SELECT DATE(date) FROM Stock_history
                        WHERE Stock_sid = s.sid AND DATE(date) <= DATE_SUB(?, INTERVAL 1 YEAR)
                        ORDER BY date DESC LIMIT 1
                    )
                ORDER BY s.stockCode
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Stock[]{
                new Stock(rs.getInt("sid"), rs.getString("stockName"),
                          rs.getString("stockCode"), rs.getBigDecimal("currentPrice")),
                new Stock(rs.getInt("sid"), rs.getString("stockName"),
                          rs.getString("stockCode"), rs.getBigDecimal("prevDayPrice")),
                new Stock(rs.getInt("sid"), rs.getString("stockName"),
                          rs.getString("stockCode"), rs.getBigDecimal("prev7Price")),
                new Stock(rs.getInt("sid"), rs.getString("stockName"),
                          rs.getString("stockCode"), rs.getBigDecimal("prev30Price")),
                new Stock(rs.getInt("sid"), rs.getString("stockName"),
                          rs.getString("stockCode"), rs.getBigDecimal("prev1YPrice"))
        // Notice there are now FIVE currentDate parameters matching the 5 '?' in the SQL
        }, currentDate, currentDate, currentDate, currentDate, currentDate);
    }
}