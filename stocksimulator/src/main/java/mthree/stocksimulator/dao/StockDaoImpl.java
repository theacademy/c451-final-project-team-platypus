/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import mthree.stocksimulator.dao.mappers.StockMapper;
import mthree.stocksimulator.model.Stock;
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
    public BigDecimal getStockPrice(int sid, String currentDate){
        String sql = "SELECT stockPrice FROM Stock_history WHERE Stock_sid = ? AND DATE(date) = ?";
        try {
            return jdbcTemplate.queryForObject(sql, BigDecimal.class, sid, currentDate);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null; // no price on this date; service handles the null
        }
    }
    
    // Populate Stock_history table with entries
    @Override
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

    @Override
    public int getOrCreateStockId(String symbol) {
        String select = "SELECT sid FROM Stock WHERE stockCode = ?";
        List<Integer> ids = jdbcTemplate.query(
                select,
                (rs, rowNum) -> rs.getInt("sid"),
                symbol);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        // Not found — create it. stockName is NOT NULL UNIQUE in the schema;
        // we don't have a human-readable name here, so reuse the symbol.
        String insert = "INSERT INTO Stock (stockName, stockCode) VALUES (?, ?)";
        org.springframework.jdbc.support.KeyHolder keyHolder =
                new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    insert, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, symbol);
            ps.setString(2, symbol);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public List<java.util.Map<String, Object>> getPriceHistory(int sid, String uptoDate) {
        String sql = """
                SELECT DATE_FORMAT(date, '%Y-%m-%d') AS date, stockPrice AS price
                FROM Stock_history
                WHERE Stock_sid = ? AND DATE(date) <= ?
                ORDER BY date ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            java.util.Map<String, Object> point = new java.util.LinkedHashMap<>();
            point.put("date", rs.getString("date"));
            point.put("price", rs.getBigDecimal("price"));
            return point;
        }, sid, uptoDate);
    }
    
    @Override
    public List<Stock> getOwnedStocks(int uid, String currentDate) {
        // Owned stocks (ownedStock > 0) joined to their price as of currentDate.
        // Price = the most recent Stock_history row on or before currentDate.
        String sql = """
                SELECT s.sid, s.stockName, s.stockCode, h.stockPrice
                FROM user_stocks us
                JOIN Stock s ON s.sid = us.Stock_sid
                JOIN Stock_history h ON h.Stock_sid = s.sid
                    AND DATE(h.date) = (
                        SELECT MAX(DATE(h2.date))
                        FROM Stock_history h2
                        WHERE h2.Stock_sid = s.sid AND DATE(h2.date) <= ?
                    )
                WHERE us.User_uid = ? AND us.ownedStock > 0
                ORDER BY s.stockCode
                """;
        return jdbcTemplate.query(sql, new StockMapper(), currentDate, uid);
    }

    @Override
    public List<Stock[]> getStocksWithPriceChange(String currentDate) {
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

            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                // Each result row carries one stock plus its 5 period prices.
                Stock current = buildStock(rs, rs.getBigDecimal("currentPrice"));
                Stock prevDay = buildStock(rs, rs.getBigDecimal("prevDayPrice"));
                Stock prev7   = buildStock(rs, rs.getBigDecimal("prev7Price"));
                Stock prev30  = buildStock(rs, rs.getBigDecimal("prev30Price"));
                Stock prevYr  = buildStock(rs, rs.getBigDecimal("prev1YPrice"));
                return new Stock[]{ current, prevDay, prev7, prev30, prevYr };
            }, currentDate, currentDate, currentDate, currentDate, currentDate);
        }

    // Helper: build a Stock with shared identity fields and a specific price.
    private Stock buildStock(java.sql.ResultSet rs, BigDecimal price) throws java.sql.SQLException {
        Stock stock = new Stock();
        stock.setSid(rs.getInt("sid"));
        stock.setStockName(rs.getString("stockName"));
        stock.setStockCode(rs.getString("stockCode"));
        stock.setStockPrice(price);
        return stock;
    }
}