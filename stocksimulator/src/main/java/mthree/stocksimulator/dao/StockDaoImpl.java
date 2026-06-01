/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mthree.stocksimulator.dao;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mthree.stocksimulator.dao.mappers.StockMapper;
import mthree.stocksimulator.dao.mappers.StockPriceSnapshotMapper;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.StockPriceSnapshot;
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
        String sql = "SELECT stockPrice FROM stock_history WHERE Stock_sid = ? AND date = ?";
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, sid, currentDate);
    }
    
    //Populate stock_history table with entries
    // use the new one in simulator cli
    @Deprecated
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
    public Map<Integer, Integer> getOwnedStocks(int uid) {
        String sql = "SELECT stock_sid, ownedStock FROM stock_history WHERE uid = ?";

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, Integer> map = new HashMap<>();
            while (rs.next()){
                map.put(rs.getInt("stock_sid"), rs.getInt("ownedStock"));
            }
            return map;
        }, uid);
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
}