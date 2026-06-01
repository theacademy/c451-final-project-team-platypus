package mthree.stocksimulator.dto;

import java.math.BigDecimal;

/**
 * Request/response payloads for the REST API. Grouped here as records to keep
 * the JSON contract explicit and decoupled from the persistence model.
 */
public final class ApiDtos {

    private ApiDtos() {}

    // ---- Auth ----

    /** Login/register payload. password is accepted but unused (username-only auth). */
    public record AuthRequest(String userName, String password, BigDecimal startingBalance) {}

    public record UserDto(int uid, String userName, BigDecimal accountBal) {}

    // ---- Stocks ----

    /** A market row: current price plus % change over each period (null if no data). */
    public record StockChangeDto(
            int sid,
            String stockCode,
            String stockName,
            BigDecimal price,
            BigDecimal change1d,
            BigDecimal change7d,
            BigDecimal change30d,
            BigDecimal change1y) {}

    /** An owned position with current price and total value. */
    public record OwnedStockDto(
            int sid,
            String stockCode,
            String stockName,
            BigDecimal price,
            int shares,
            BigDecimal value) {}

    // ---- Trading ----

    public record TradeRequest(int uid, int sid, int quantity) {}

    public record TradeResult(boolean success, String message) {}

    // ---- Simulation state ----

    public record PortfolioDto(
            String date,
            BigDecimal cash,
            BigDecimal stockValue,
            BigDecimal total,
            boolean simulationOver) {}
}