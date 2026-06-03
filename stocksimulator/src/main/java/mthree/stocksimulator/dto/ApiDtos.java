package mthree.stocksimulator.dto;

import java.math.BigDecimal;

/**
 * Request/response payloads for the REST API. Grouped here as records to keep
 * the JSON contract explicit and decoupled from the persistence model.
 */
public final class ApiDtos {

    private ApiDtos() {}

    // ---- Auth ----

    /** Login/register payload. */
    public record AuthRequest(String userName, BigDecimal startingBalance) {}

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