package mthree.stocksimulator.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;
import mthree.stocksimulator.model.Stock;
import mthree.stocksimulator.model.User;
import mthree.stocksimulator.service.SimServiceImpl;
import mthree.stocksimulator.service.UserServiceImpl;

/**
 *
 * @author jerem
 */
@Component
@Profile("cli")
public class SimulatorCLI implements CommandLineRunner {

    private final SimServiceImpl stockService;
    private final UserServiceImpl userService;
    private final Scanner scanner = new Scanner(System.in);
    
    private final String[] stocks = {
                                    "AAPL", "MSFT", "IBM", "NVDA", "DELL", // tech companies
                                    // "TSLA"
                                    };

    private User currentUser;

    public SimulatorCLI(SimServiceImpl stockService, UserServiceImpl userService) {
        this.stockService = stockService;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Stock Simulator ===");
        loginOrCreateUser();

        stockService.loadTradingDays();

        if (stockService.tradingDays == null || stockService.tradingDays.isEmpty()) {
            System.out.println("No trading data found. Fetching stock data now (this may take a while)...");
            for (String symbol : stocks) {
                stockService.fetchAndStoreSymbol(symbol);
            }
            stockService.loadTradingDays(); // reload after fetch
        }

        if (stockService.tradingDays == null || stockService.tradingDays.isEmpty()) {
            System.out.println("Failed to load trading data. Check API key or network connection.");
            return;
        }

        mainMenu();
    }

    // -------------------------
    // User Login / Creation
    // -------------------------
    private void loginOrCreateUser() {
        System.out.println("\n1. Login with existing user ID");
        System.out.println("2. Create new user");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("Enter your user ID: ");
            int uid = Integer.parseInt(scanner.nextLine().trim());
            currentUser = userService.getUser(uid);
            System.out.println("Welcome back, " + currentUser.getUserName() + "!");
        } else {
            System.out.print("Enter username: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter starting balance: ");
            BigDecimal balance = new BigDecimal(scanner.nextLine().trim());
            currentUser = userService.createUser(name, balance);
            System.out.println("Account created! Welcome, " + currentUser.getUserName() + "!");
        }
    }

    // -------------------------
    // Main Menu
    // -------------------------
    private void mainMenu() {
        boolean running = true;

        while (running) {
            currentUser = userService.getUser(currentUser.getUid());

            if (stockService.isSimulationOver()) {
                showFinalResults();
                running = false;
                break;
            }

            // Calculate total stock value
            List<Stock> ownedStocks = stockService.getOwnedStocks(currentUser.getUid());
            BigDecimal totalStockValue = BigDecimal.ZERO;
            for (Stock stock : ownedStocks) {
                int shares = stockService.getOwnedShares(currentUser.getUid(), stock.getSid());
                BigDecimal value = stock.getStockPrice()
                        .multiply(BigDecimal.valueOf(shares))
                        .setScale(2, RoundingMode.HALF_UP);
                totalStockValue = totalStockValue.add(value);
            }

            BigDecimal totalPortfolio = currentUser.getAccountBal().add(totalStockValue);

            System.out.println("\n==============================");
            System.out.println("  Date:              " + stockService.getDate());
            System.out.println("  Cash Balance:     $" + currentUser.getAccountBal());
            System.out.println("  Stock Value:      $" + totalStockValue.setScale(2, RoundingMode.HALF_UP));
            System.out.println("  Portfolio Total:  $" + totalPortfolio.setScale(2, RoundingMode.HALF_UP));
            System.out.println("==============================");
            System.out.println("1. View & Buy Stocks");
            System.out.println("2. Sell Stocks");
            System.out.println("3. Advance Time");
            System.out.println("4. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewAndBuyStocks();
                case "2" -> sellStocks();
                case "3" -> advanceTime();
                case "4" -> running = false;
                default  -> System.out.println("Invalid option, please try again.");
            }
        }

        System.out.println("Goodbye, " + currentUser.getUserName() + "!");
    }

    // -------------------------
    // View & Buy Stocks
    // -------------------------
    private void viewAndBuyStocks() {
        List<Stock[]> stocksWithChange = stockService.getStocksWithPriceChange();

        if (stocksWithChange.isEmpty()) {
            System.out.println("No stock data available for the current date.");
            return;
        }

        System.out.println("\n--- Available Stocks (" + stockService.getDate() + ") ---");
        System.out.printf("%-5s %-10s %-12s %-10s %-10s %-10s %-10s%n",
                "#", "Code", "Price", "1D", "7D", "30D", "1Y");
        System.out.println("-".repeat(77));

        for (int i = 0; i < stocksWithChange.size(); i++) {
            Stock current  = stocksWithChange.get(i)[0];
            Stock prevDay  = stocksWithChange.get(i)[1];
            Stock prev7    = stocksWithChange.get(i)[2];
            Stock prev30   = stocksWithChange.get(i)[3];
            Stock prevYear = stocksWithChange.get(i)[4];

            System.out.printf("%-5d %-10s $%-12s %-10s %-10s %-10s %-10s%n",
                    i + 1,
                    current.getStockCode(),
                    current.getStockPrice(),
                    calcChange(current.getStockPrice(), prevDay.getStockPrice()),
                    calcChange(current.getStockPrice(), prev7.getStockPrice()),
                    calcChange(current.getStockPrice(), prev30.getStockPrice()),
                    calcChange(current.getStockPrice(), prevYear.getStockPrice()));
        }

        System.out.println("\nEnter the number of the stock to buy, or 0 to go back:");
        System.out.print("Choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice == 0) return;
        if (choice < 1 || choice > stocksWithChange.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Stock selected = stocksWithChange.get(choice - 1)[0];
        System.out.print("How many shares of " + selected.getStockCode() + " would you like to buy? ");

        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        String result = stockService.buyStock(currentUser.getUid(), selected.getSid(), quantity);
        System.out.println(result);
    }

    // Helper to calculate and format percentage change
    private String calcChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return "N/A";
        }
        BigDecimal change = current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        return (change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + change + "%";
    }
    // -------------------------
    // Sell Stocks
    // -------------------------
    private void sellStocks() {
        List<Stock> ownedStocks = stockService.getOwnedStocks(currentUser.getUid());

        if (ownedStocks.isEmpty()) {
            System.out.println("You don't own any stocks.");
            return;
        }

        System.out.println("\n--- Your Stocks ---");
        System.out.printf("%-5s %-20s %-10s %-10s %-10s%n", "#", "Name", "Code", "Price", "Owned");
        System.out.println("-".repeat(60));

        for (int i = 0; i < ownedStocks.size(); i++) {
            Stock s = ownedStocks.get(i);
            int owned = stockService.getOwnedShares(currentUser.getUid(), s.getSid());
            System.out.printf("%-5d %-20s %-10s $%-10s %-10d%n",
                    i + 1, s.getStockName(), s.getStockCode(), s.getStockPrice(), owned);
        }

        System.out.println("\nEnter the number of the stock to sell, or 0 to go back:");
        System.out.print("Choice: ");

        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice == 0) return;
        if (choice < 1 || choice > ownedStocks.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Stock selected = ownedStocks.get(choice - 1);
        System.out.print("How many shares of " + selected.getStockCode() + " would you like to sell? ");

        int quantity;
        try {
            quantity = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        String result = stockService.sellStock(currentUser.getUid(), selected.getSid(), quantity);
        System.out.println(result);
    }

    // -------------------------
    // Advance Time
    // -------------------------
    private void advanceTime() {
        System.out.print("How many trading days would you like to advance? ");
        int days;
        try {
            days = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        stockService.advanceTime(days);
        System.out.println("Date is now: " + stockService.getDate());

        if (stockService.isSimulationOver()) {
            showFinalResults();
        }
    }

    private void showFinalResults() {
        System.out.println("\n========================================");
        System.out.println("         SIMULATION COMPLETE");
        System.out.println("========================================");
        System.out.println("  Final Date: " + stockService.getDate());

        // Refresh user for latest balance
        currentUser = userService.getUser(currentUser.getUid());
        System.out.println("  Cash Balance: $" + currentUser.getAccountBal());

        // Get owned stocks and their current value
        List<Stock> ownedStocks = stockService.getOwnedStocks(currentUser.getUid());

        BigDecimal totalStockValue = BigDecimal.ZERO;

        if (ownedStocks.isEmpty()) {
            System.out.println("  No stocks held.");
        } else {
            System.out.println("\n  Holdings:");
            System.out.printf("  %-15s %-10s %-12s %-12s%n", "Stock", "Shares", "Price", "Value");
            System.out.println("  " + "-".repeat(52));

            for (Stock stock : ownedStocks) {
                int shares = stockService.getOwnedShares(currentUser.getUid(), stock.getSid());
                BigDecimal value = stock.getStockPrice()
                        .multiply(BigDecimal.valueOf(shares))
                        .setScale(2, RoundingMode.HALF_UP);
                totalStockValue = totalStockValue.add(value);

                System.out.printf("  %-15s %-10d $%-12s $%-12s%n",
                        stock.getStockCode(), shares, stock.getStockPrice(), value);
            }
        }

        BigDecimal totalPortfolio = currentUser.getAccountBal().add(totalStockValue);
        System.out.println("\n  Total Stock Value: $" + totalStockValue.setScale(2, RoundingMode.HALF_UP));
        System.out.println("  Total Portfolio Value: $" + totalPortfolio.setScale(2, RoundingMode.HALF_UP));
        System.out.println("========================================");
    }
}