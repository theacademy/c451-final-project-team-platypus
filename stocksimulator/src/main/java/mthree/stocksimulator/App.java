/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package mthree.stocksimulator;


import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import mthree.stocksimulator.dao.StockDao;
import mthree.stocksimulator.dao.mappers.StockMapper;
import mthree.stocksimulator.model.Stock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class
})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
    
    @Component
    public static class OnStartup implements CommandLineRunner {
        
        @Value("${api.key}")
        private String API_KEY;
        
        private JdbcTemplate jdbcTemplate;
        private final WebClient webClient;
        private final StockDao stockDao;
        private static final String BASE_URL = "https://www.alphavantage.co";
        private static final String START_DATE = "2000-01-01";
        private static final String END_DATE = "2020-01-01";

        
        public OnStartup(JdbcTemplate jdbcTemplate, StockDao stockDao, WebClient.Builder webClientBuilder) {
            this.jdbcTemplate = jdbcTemplate;
            this.stockDao = stockDao;
            this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(32 * 1024 * 1024)) // 32MB buffer
                .build();
        }
        
        @Override
        public void run(String... args){
            List<Stock> allStocks = jdbcTemplate.query("SELECT * FROM Stock", new StockMapper());
            try {
                for (Stock stock : allStocks) {
                    fetchAndStoreSymbolData(stock);
                }
            } catch (RuntimeException e) {
                System.err.println("Encountered error while loading data from API! Terminating!");
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
        
        public void fetchAndStoreSymbolData(Stock stock) throws RuntimeException{
            System.out.println("Fetching data for " + stock.getStockCode() + " via WebClient...");

            // Fetch and parse via WebClient
            JsonNode rootNode = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/query")
                            .queryParam("function", "TIME_SERIES_DAILY")
                            .queryParam("symbol", stock.getStockCode())
                            .queryParam("outputsize", "full")
                            .queryParam("apikey", API_KEY)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.value() == 429, response -> Mono.error(new RuntimeException("Too many requests!")))
                    .onStatus(status -> status.isError(), response -> Mono.error(new RuntimeException("Got HTTP Error: " + response.statusCode())))
                    .bodyToMono(JsonNode.class)
                    .block();

            // Extract and filter the time series data
            JsonNode timeSeriesNode = rootNode.get("Time Series (Daily)");
            List<Object[]> historyRows = new ArrayList<>();

            timeSeriesNode.fields().forEachRemaining(entry -> {
                String date = entry.getKey();
                if (date.compareTo(START_DATE) >= 0 && date.compareTo(END_DATE) <= 0) {
                    BigDecimal price = new BigDecimal(entry.getValue().get("1. open").asText())
                            .setScale(4, RoundingMode.HALF_UP);
                    // Store as [stockSid, date, price] for batch insert
                    historyRows.add(new Object[]{stock.getSid(), LocalDateTime.parse(date + "T00:00:00"), price});
                }
            });

            // Batch insert into Stock_history
            if (!historyRows.isEmpty()) {
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
                System.out.println("-> Successfully inserted " + historyRows.size() + " history rows for " + stock.getStockCode());
            } else {
                System.out.println("-> No valid dates found in the specified range for " + stock.getStockCode());
            }
    }
    }
}
