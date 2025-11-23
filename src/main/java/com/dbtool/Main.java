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
import com.dbtool.core.PaginationUtil;

import javax.swing.*;  // 添加 Swing 导入
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

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
        // 检查启动参数
        if (args.length > 0 && (args[0].equals("--gui") || args[0].equals("-g"))) {
            startGUI();
        } else if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            showStartupHelp();
        } else {
            startCLI();
        }
    }

    private static void startGUI() {
        System.out.println("Launching graphical interface mode...");
        try {
            // 尝试加载GUI类
            SwingUtilities.invokeLater(() -> {
                try {
                    new com.dbtool.DBManagerGUI();
                } catch (Exception e) {
                    System.err.println("GUI startup failed: " + e.getMessage());
                    System.out.println("Switch to command line mode...");
                    startCLI();
                }
            });
        } catch (Exception e) {
            System.err.println("Unable to start the graphical user interface: " + e.getMessage());
            System.out.println("Switch to command line mode...");
            startCLI();
        }
    }

    // 将原有的main方法内容移到startCLI方法中
    private static void startCLI() {
        System.out.println("Starting Database Management Tool...");

        initialize();
        showWelcome();
        runCommandLoop();
        cleanup();

        System.out.println("👋 Thank you for using Database Management Tool!");
    }

    private static void showStartupHelp() {
        System.out.println("Database Management Tool User Guide:");
        System.out.println("  java -jar DBManagerTool.jar         ");
        System.out.println("  java -jar DBManagerTool.jar --gui   ");
        System.out.println("  java -jar DBManagerTool.jar --help  ");
        System.out.println();
        System.out.println("or use the classpath approach:");
        System.out.println("  java -cp \"DBManagerTool.jar;lib/*\" com.dbtool.Main --gui");
    }

    // 原有的initialize方法保持不变
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
                showCommandHelp();
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

            // 新增的命令类型
            case GET_TABLES:
                getTablesViaExecutor();
                break;

            case VALIDATE_SQL:
                validateSQL();
                break;

            case PREPARED_QUERY:
                executePreparedQuery();
                break;

            case BATCH_EXECUTE:
                executeBatchSQL();
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

            // 分页功能
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

    private static void handlePaginationInput(String input) {
        // 暂时禁用分页模式
        inPaginationMode = false;
        currentPage = 1;
        ColorPrinter.printInfo("Pagination mode disabled in current version");

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

    // ========== 新增的方法：实际使用 SQLExecutor 的新功能 ==========

    /**
     * 使用 SQLExecutor 的 getTables 方法获取表列表
     */
    private static void getTablesViaExecutor() {
        ColorPrinter.printHeader("Getting Tables via SQLExecutor");
        QueryResult result = sqlExecutor.getTables(dbManager.getConnection());
        TableFormatter.displayResult(result);
    }

    /**
     * 使用 SQLExecutor 的 validateSQL 方法验证 SQL 语法
     */
    private static void validateSQL() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter SQL to validate: ");
        String sql = scanner.nextLine().trim();

        if (!sql.isEmpty()) {
            ColorPrinter.printInfo("Validating SQL: " + sql);
            QueryResult result = sqlExecutor.validateSQL(dbManager.getConnection(), sql);
            ColorPrinter.printSuccess(result.getMessage());
        } else {
            ColorPrinter.printError("No SQL provided for validation");
        }
    }

    /**
     * 使用 SQLExecutor 的 executePrepared 方法执行参数化查询
     */
    private static void executePreparedQuery() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Parameterized Query Demo ===");
        System.out.println("Example: SELECT * FROM users WHERE age > ? AND name LIKE ?");

        try {
            List<Object> params = new ArrayList<>();

            System.out.print("Enter minimum age: ");
            String ageInput = scanner.nextLine().trim();
            if (!ageInput.isEmpty()) {
                params.add(Integer.parseInt(ageInput));
            }

            System.out.print("Enter name pattern (e.g., %John%): ");
            String nameInput = scanner.nextLine().trim();
            if (!nameInput.isEmpty()) {
                params.add(nameInput);
            }

            // 根据参数数量构建SQL
            String sql;
            if (params.size() == 2) {
                sql = "SELECT * FROM users WHERE age > ? AND name LIKE ?";
            } else if (params.size() == 1) {
                if (ageInput.isEmpty()) {
                    sql = "SELECT * FROM users WHERE name LIKE ?";
                } else {
                    sql = "SELECT * FROM users WHERE age > ?";
                }
            } else {
                sql = "SELECT * FROM users LIMIT 10";
            }

            ColorPrinter.printSQL("Executing: " + sql);
            QueryResult result = sqlExecutor.executePrepared(dbManager.getConnection(), sql, params);
            TableFormatter.displayResult(result);

        } catch (Exception e) {
            ColorPrinter.printError("Error in prepared query: " + e.getMessage());
        }
    }

    /**
     * 使用 SQLExecutor 的 executeMultiple 方法执行批量 SQL
     */
    private static void executeBatchSQL() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Batch SQL Execution ===");
        System.out.println("Enter multiple SQL statements separated by semicolons:");
        System.out.println("Example: SHOW TABLES; SELECT NOW(); SELECT 1 + 1;");

        String batchInput = scanner.nextLine().trim();

        if (!batchInput.isEmpty()) {
            ColorPrinter.printInfo("Executing batch SQL...");
            List<QueryResult> results = sqlExecutor.executeMultiple(dbManager.getConnection(), batchInput);

            System.out.println("\n=== Batch Execution Results ===");
            for (int i = 0; i < results.size(); i++) {
                QueryResult result = results.get(i);
                System.out.println("Statement " + (i + 1) + ": " + result.getMessage());
                if (result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
                    TableFormatter.displayResult(result);
                }
            }
        } else {
            ColorPrinter.printError("No SQL provided for batch execution");
        }
    }

    // 重命名方法，避免重复
    private static void showCommandHelp() {
        ColorPrinter.printHeader("Available Commands");

        System.out.println("\nMETA COMMANDS (start with \\):");
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

        // 新增的命令
        System.out.println("  \\get_tables              - Get tables via SQLExecutor");
        System.out.println("  \\validate                - Validate SQL syntax");
        System.out.println("  \\prepared                - Execute parameterized query");
        System.out.println("  \\batch                   - Execute batch SQL statements");
        System.out.println("  \\help                    - Show this help");

        System.out.println("\nBUILT-IN COMMANDS:");
        System.out.println("  help                     - Show help");
        System.out.println("  status                   - Show connection status");
        System.out.println("  test                     - Test database connection");
        System.out.println("  config                   - Show configuration");
        System.out.println("  exit                     - Exit the program");

        System.out.println("\nPAGINATION COMMANDS:");
        System.out.println("  P                        - Previous page");
        System.out.println("  N                        - Next page");
        System.out.println("  Q                        - Quit paging mode");
        System.out.println("  <number>                 - Go to specific page");

        System.out.println("\nSQL COMMANDS:");
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
        System.out.println("  Database: " + (dbManager.isConnected() ? "Connected" : "Disconnected"));
        System.out.println("  " + dbManager.getConnectionInfo());

        if (dbManager.testConnection()) {
            System.out.println("  Connection test: Valid");
        } else {
            System.out.println("  Connection test: Invalid");
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