package com.dbtool;

import com.dbtool.command.CommandParser;
import com.dbtool.core.DatabaseManager;
import com.dbtool.core.MetadataManager;
import com.dbtool.core.SQLExecutor;
import com.dbtool.core.HistoryManager;
import com.dbtool.model.QueryResult;
import com.dbtool.util.ColorPrinter;
import com.dbtool.util.ConfigLoader;
import com.dbtool.util.TableFormatter;
import com.dbtool.util.ExportUtil;
import com.dbtool.util.PaginationUtil;

import java.util.Scanner;

public class Main {
    private static DatabaseManager dbManager;
    private static SQLExecutor sqlExecutor;
    private static MetadataManager metadataManager;
    private static HistoryManager historyManager;
    private static boolean running = true;
    private static QueryResult lastQueryResult;
    private static int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private static boolean inPaginationMode = false;

    public static void main(String[] args) {
        System.out.println("🚀 Starting Database Management Tool...");

        initialize();
        showWelcome();
        runCommandLoop();
        cleanup();

        System.out.println("👋 Thank you for using Database Management Tool!");
    }

    private static void initialize() {
        ConfigLoader.printConfig();
        dbManager = new DatabaseManager();
        sqlExecutor = new SQLExecutor();
        historyManager = new HistoryManager();

        if (!dbManager.connect()) {
            ColorPrinter.printError("Failed to connect to database. Exiting...");
            System.exit(1);
        }

        metadataManager = new MetadataManager(dbManager.getConnection());
        ColorPrinter.printSuccess("All components initialized successfully");
    }

    private static void showWelcome() {
        System.out.println("\n" +
                "╔══════════════════════════════════════════════╗\n" +
                "║           DATABASE MANAGEMENT TOOL           ║\n" +
                "║                   Day 4 ✅                   ║\n" +
                "╚══════════════════════════════════════════════╝\n");

        System.out.println(dbManager.getConnectionInfo());
        ColorPrinter.printInfo("Type 'help' for available commands, 'exit' to quit.");
    }

    private static void runCommandLoop() {
        Scanner scanner = new Scanner(System.in);

        while (running) {
            if (inPaginationMode && lastQueryResult != null) {
                System.out.print("\nPage> ");
            } else {
                System.out.print("\nDB> ");
            }

            String input = scanner.nextLine().trim();

            if (inPaginationMode) {
                handlePaginationInput(input);
            } else {
                CommandParser.CommandResult command = CommandParser.parse(input);
                handleCommand(command);
            }
        }

        scanner.close();
    }

    private static void handleCommand(CommandParser.CommandResult command) {
        if (!PaginationUtil.isPaginationCommand(command.getContent()) &&
                command.getType() != CommandParser.CommandType.PAGINATION) {
            historyManager.addToHistory(command.getContent());
        }

        switch (command.getType()) {
            case EXIT:
                running = false;
                break;

            case HELP:
                showHelp();
                break;

            case STATUS:
                showStatus();
                break;

            case TEST_CONNECTION:
                testConnection();
                break;

            case SHOW_CONFIG:
                ConfigLoader.printConfig();
                break;

            case LIST_DATABASES:
                listDatabases();
                break;

            case LIST_TABLES:
                listTables(command.getContent());
                break;

            case DESCRIBE_TABLE:
                describeTable(command.getContent());
                break;

            case DATABASE_INFO:
                showDatabaseInfo();
                break;

            case TABLE_STATUS:
                showTableStatus();
                break;

            case USE_DATABASE:
                useDatabase(command.getContent());
                break;

            case HISTORY:
                showHistory(command.getContent());
                break;

            case CLEAR_HISTORY:
                clearHistory();
                break;

            case EXPORT_CSV:
                exportToCSV(command.getContent());
                break;

            case EXPORT_TEXT:
                exportToText(command.getContent());
                break;

            case SQL:
                executeSQL(command.getContent());
                break;

            case ERROR:
                ColorPrinter.printError(command.getContent());
                break;

            case EMPTY:
                break;

            default:
                ColorPrinter.printWarning("Unknown command type: " + command.getType());
                break;
        }
    }

    private static void executeSQL(String sql) {
        ColorPrinter.printSQL(sql);
        QueryResult result = sqlExecutor.execute(dbManager.getConnection(), sql);

        if (result.isSuccess() && result.isQueryResult() && result.getData() != null) {
            lastQueryResult = result;

            if (result.getData().size() > PAGE_SIZE) {
                inPaginationMode = true;
                currentPage = 1;
                ColorPrinter.printInfo("Large result set. Entering pagination mode.");
                PaginationUtil.displayPaginatedResult(result, currentPage, PAGE_SIZE);
            } else {
                TableFormatter.displayResult(result);
            }
        } else {
            TableFormatter.displayResult(result);
        }
    }

    private static void listDatabases() {
        ColorPrinter.printHeader("Listing Databases");
        QueryResult result = metadataManager.getDatabases();
        TableFormatter.displayResult(result);
    }

    private static void listTables(String databaseName) {
        if (databaseName.isEmpty()) {
            ColorPrinter.printHeader("Listing Tables in Current Database");
            QueryResult result = metadataManager.getTables();
            TableFormatter.displayResult(result);
        } else {
            ColorPrinter.printHeader("Listing Tables in Database: " + databaseName);
            QueryResult result = metadataManager.getTables(databaseName);
            TableFormatter.displayResult(result);
        }
    }

    private static void describeTable(String tableName) {
        ColorPrinter.printHeader("Describing Table: " + tableName);
        QueryResult result = metadataManager.describeTable(tableName);
        TableFormatter.displayResult(result);
    }

    private static void showDatabaseInfo() {
        ColorPrinter.printHeader("Database Information");
        QueryResult result = metadataManager.getDatabaseInfo();
        TableFormatter.displayResult(result);
    }

    private static void showTableStatus() {
        ColorPrinter.printHeader("Table Status");
        QueryResult result = metadataManager.getTableStatus();
        TableFormatter.displayResult(result);
    }

    private static void useDatabase(String databaseName) {
        String sql = "USE " + databaseName;
        ColorPrinter.printInfo("Switching to database: " + databaseName);
        QueryResult result = sqlExecutor.execute(dbManager.getConnection(), sql);
        TableFormatter.displayResult(result);
    }

    private static void showHistory(String argument) {
        int count = 10;
        try {
            if (!argument.isEmpty()) {
                count = Integer.parseInt(argument);
            }
        } catch (NumberFormatException e) {
            ColorPrinter.printError("Invalid count: " + argument);
            return;
        }

        historyManager.showHistory(count);
    }

    private static void clearHistory() {
        historyManager.clearHistory();
        ColorPrinter.printSuccess("Command history cleared");
    }

    private static void exportToCSV(String filename) {
        if (lastQueryResult == null) {
            ColorPrinter.printError("No query result to export. Please run a query first.");
            return;
        }

        ExportUtil.exportToCSV(lastQueryResult, filename);
    }

    private static void exportToText(String filename) {
        if (lastQueryResult == null) {
            ColorPrinter.printError("No query result to export. Please run a query first.");
            return;
        }

        ExportUtil.exportToText(lastQueryResult, filename);
    }

    private static void handlePaginationInput(String input) {
        String lowerInput = input.trim().toLowerCase();

        switch (lowerInput) {
            case "p":
                if (currentPage > 1) {
                    currentPage--;
                }
                break;

            case "n":
                currentPage++;
                break;

            case "q":
                inPaginationMode = false;
                currentPage = 1;
                return;

            default:
                try {
                    int page = Integer.parseInt(input);
                    if (page >= 1) {
                        currentPage = page;
                    }
                } catch (NumberFormatException e) {
                    ColorPrinter.printError("Invalid page command. Use P, N, Q, or page number.");
                    return;
                }
        }

        PaginationUtil.displayPaginatedResult(lastQueryResult, currentPage, PAGE_SIZE);
    }

    private static void showHelp() {
        ColorPrinter.printHeader("Available Commands");

        System.out.println("\n📋 META COMMANDS (start with \\):");
        System.out.println("  \\l or \\list              - List all databases");
        System.out.println("  \\t or \\tables            - List tables in current database");
        System.out.println("  \\t <db> or \\tables <db>  - List tables in specific database");
        System.out.println("  \\d or \\desc <table>      - Describe table structure");
        System.out.println("  \\info                    - Show database information");
        System.out.println("  \\status                  - Show table status");
        System.out.println("  \\use <database>          - Switch to another database");
        System.out.println("  \\history [count]         - Show command history");
        System.out.println("  \\clear_history           - Clear command history");
        System.out.println("  \\export <file>           - Export last result to CSV");
        System.out.println("  \\export_csv <file>       - Export last result to CSV");
        System.out.println("  \\export_txt <file>       - Export last result to text");
        System.out.println("  \\help                    - Show this help");

        System.out.println("\n💡 BUILT-IN COMMANDS:");
        System.out.println("  help                     - Show help");
        System.out.println("  status                   - Show connection status");
        System.out.println("  test                     - Test database connection");
        System.out.println("  config                   - Show configuration");
        System.out.println("  exit                     - Exit the program");

        System.out.println("\n📄 PAGINATION COMMANDS:");
        System.out.println("  P                        - Previous page");
        System.out.println("  N                        - Next page");
        System.out.println("  Q                        - Quit paging mode");
        System.out.println("  <number>                 - Go to specific page");

        System.out.println("\n🔧 SQL COMMANDS:");
        System.out.println("  Any SQL statement        - Execute SQL commands");
        System.out.println("  Examples:");
        System.out.println("    SELECT * FROM table;");
        System.out.println("    SHOW DATABASES;");
        System.out.println("    CREATE TABLE ...;");
        System.out.println("    INSERT INTO ...;");
        System.out.println("    UPDATE ...;");
        System.out.println("    DELETE FROM ...;");
    }

    private static void showStatus() {
        ColorPrinter.printHeader("Application Status");
        System.out.println("  Database: " + (dbManager.isConnected() ? "✅ Connected" : "❌ Disconnected"));
        System.out.println("  " + dbManager.getConnectionInfo());

        if (dbManager.testConnection()) {
            System.out.println("  Connection test: ✅ Valid");
        } else {
            System.out.println("  Connection test: ❌ Invalid");
        }
    }

    private static void testConnection() {
        System.out.print("Testing connection... ");
        if (dbManager.testConnection()) {
            ColorPrinter.printSuccess("SUCCESS");
        } else {
            ColorPrinter.printError("FAILED");
        }
    }

    private static void cleanup() {
        if (dbManager != null) {
            dbManager.disconnect();
        }
    }
}