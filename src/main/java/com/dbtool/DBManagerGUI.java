package com.dbtool;

import com.dbtool.core.DatabaseManager;
import com.dbtool.core.SQLExecutor;
import com.dbtool.core.MetadataManager;
import com.dbtool.core.HistoryManager;
import com.dbtool.model.QueryResult;
import com.dbtool.util.TableFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class DBManagerGUI extends JFrame {
    private DatabaseManager dbManager;
    private SQLExecutor sqlExecutor;
    private MetadataManager metadataManager;
    private HistoryManager historyManager;


    // UI components
    private JTextArea outputArea;
    private JTextField inputField;
    private JButton executeButton;
    private JButton clearButton;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    public DBManagerGUI() {
        initializeDatabase();
        initializeGUI();
    }

    private void initializeDatabase() {
        try {
            dbManager = new DatabaseManager();
            if (!dbManager.connect()) {
                JOptionPane.showMessageDialog(this,
                        "Database connection failed! Please check if database service is running.",
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            sqlExecutor = new SQLExecutor();
            metadataManager = new MetadataManager(dbManager.getConnection());
            historyManager = new HistoryManager();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Initialization failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeGUI() {
        setTitle("Database Management Tool - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        outputArea.setBackground(new Color(240, 240, 240));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setPreferredSize(new Dimension(0, 200));

        // Result table
        tableModel = new DefaultTableModel();
        JTable resultTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(resultTable);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        inputField.setFont(new Font("Consolas", Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        executeButton = new JButton("Execute (Enter)");
        clearButton = new JButton("Clear");
        buttonPanel.add(executeButton);
        buttonPanel.add(clearButton);

        inputPanel.add(new JLabel("SQL Command:"), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // Status bar
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

        // Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputScroll, tableScroll);
        splitPane.setResizeWeight(0.3);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(createToolbar(), BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Event handlers
        setupEventHandlers();

        setVisible(true);
        appendOutput("Database Management Tool - GUI Started\n");
        appendOutput("Database connected successfully\n");
        appendOutput("Tip: Enter SQL commands or use toolbar shortcuts\n");
    }

    private JToolBar createToolbar() {
    JToolBar toolbar = new JToolBar();
    toolbar.setFloatable(false);

    String[] commands = {
            "Show Databases", "SHOW DATABASES",
            "Show Tables", "SHOW TABLES",
            "Describe Table", "DESCRIBE_TABLE",
            "Database Info", "DATABASE_INFO",
            "Table Status", "TABLE_STATUS",
            "Show History", "SHOW_HISTORY",
            "Show Recent", "SHOW_RECENT",
            "Search History", "SEARCH_HISTORY",
            "Clear History", "CLEAR_HISTORY",
            "Status", "STATUS",
            "Test Connection", "TEST"
    };

    for (int i = 0; i < commands.length; i += 2) {
        JButton button = new JButton(commands[i]);
        final String command = commands[i + 1];
        button.addActionListener(e -> executeCommand(command));
        toolbar.add(button);
    }

    return toolbar;
}


    private void setupEventHandlers() {
        executeButton.addActionListener(this::executeCurrentCommand);
        clearButton.addActionListener(e -> {
            outputArea.setText("");
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            statusLabel.setText("Cleared");
        });
        inputField.addActionListener(this::executeCurrentCommand);
    }

    private void executeCurrentCommand(ActionEvent e) {
        String command = inputField.getText().trim();
        if (!command.isEmpty()) {
            executeCommand(command);
            inputField.setText("");
        }
    }

    private void executeCommand(String command) {
    if (!command.equalsIgnoreCase("SHOW_HISTORY") &&
        !command.equalsIgnoreCase("CLEAR_HISTORY")) {
        historyManager.add(command);
    }

    appendOutput("> " + command + "\n");

    try {
        if (command.equalsIgnoreCase("status")) {
            // Handle status command
            appendOutput("Connection Info: " + dbManager.getConnectionInfo() + "\n");
            boolean success = dbManager.testConnection();
            appendOutput(success ? "Connection test successful\n" : "Connection test failed\n");
            statusLabel.setText("Status checked");
        } else if (command.equalsIgnoreCase("test")) {
            // Test connection
            boolean success = dbManager.testConnection();
            appendOutput(success ? "Connection test successful\n" : "Connection test failed\n");
            statusLabel.setText("Test completed");
        } else if (command.equalsIgnoreCase("SHOW_DATABASES")) {
            // Show databases using metadata manager
            QueryResult result = metadataManager.getDatabases();
            displayResult(result);
        } else if (command.equalsIgnoreCase("SHOW_TABLES")) {
            // Show tables using metadata manager
            QueryResult result = metadataManager.getTables();
            displayResult(result);
        } else if (command.equalsIgnoreCase("DESCRIBE_TABLE")) {
            // Prompt user for table name
            String tableName = JOptionPane.showInputDialog(this, "Enter table name:");
            if (tableName != null && !tableName.trim().isEmpty()) {
                QueryResult result = metadataManager.describeTable(tableName.trim());
                displayResult(result);
            }
        } else if (command.equalsIgnoreCase("DATABASE_INFO")) {
            // Show database info
            QueryResult result = metadataManager.getDatabaseInfo();
            displayResult(result);
        } else if (command.equalsIgnoreCase("TABLE_STATUS")) {
            // Show table status
            QueryResult result = metadataManager.getTableStatus();
            displayResult(result);
        } else if (command.equalsIgnoreCase("SHOW_HISTORY")) {
            // Show command history
            showHistory();
        } else if (command.equalsIgnoreCase("CLEAR_HISTORY")) {
            // Clear command history
            historyManager.clear();
            appendOutput("Command history cleared\n");
            statusLabel.setText("History cleared");
        } else if (command.equalsIgnoreCase("SHOW_RECENT")) {
            // Show recent command history (last 10 items)
            showRecentHistory(10);
        } else if (command.equalsIgnoreCase("SEARCH_HISTORY")) {
            // Prompt user for search term
            String keyword = JOptionPane.showInputDialog(this, "Enter search keyword:");
            if (keyword != null && !keyword.trim().isEmpty()) {
                searchHistory(keyword.trim());
            }
        } else {
            // Execute SQL
            QueryResult result = sqlExecutor.execute(dbManager.getConnection(), command);
            displayResult(result);
        }
    } catch (Exception e) {
        appendOutput("Error: " + e.getMessage() + "\n");
        statusLabel.setText("Execution failed");
    }
}
    private void showRecentHistory(int count) {
        List<String> recent = historyManager.getRecent(count);
        if (recent.isEmpty()) {
            appendOutput("No recent command history available\n");
        } else {
            appendOutput("=== Recent Command History (Last " + count + ") ===\n");
            for (int i = 0; i < recent.size(); i++) {
                appendOutput(String.format("%3d. %s\n", i + 1, recent.get(i)));
            }
            appendOutput("================================================\n");
        }
        statusLabel.setText("Showing recent history (" + recent.size() + " items)");
    }

    private void searchHistory(String keyword) {
        List<String> results = historyManager.search(keyword);
        if (results.isEmpty()) {
            appendOutput("No matching commands found for: " + keyword + "\n");
        } else {
            appendOutput("=== Search Results for '" + keyword + "' ===\n");
            for (int i = 0; i < results.size(); i++) {
                appendOutput(String.format("%3d. %s\n", i + 1, results.get(i)));
            }
            appendOutput("=========================================\n");
        }
        statusLabel.setText("Search completed (" + results.size() + " matches)");
    }

    private void showHistory() {
        List<String> history = historyManager.getAll();
        if (history.isEmpty()) {
            appendOutput("No command history available\n");
        } else {
            appendOutput("=== Command History ===\n");
            for (int i = 0; i < history.size(); i++) {
                appendOutput(String.format("%3d. %s\n", i + 1, history.get(i)));
            }
            appendOutput("=======================\n");
        }
        statusLabel.setText("Showing history (" + history.size() + " items)");
    }


    private void displayResult(QueryResult result) {
        if (result.isSuccess()) {
            if (result.getData() != null && !result.getData().isEmpty()) {
                // Display table data
                displayTableResult(result);
                appendOutput("Query successful: " + result.getData().size() + " rows returned\n");
            } else {
                appendOutput("Success: " + result.getMessage() + "\n");
            }
            statusLabel.setText("Execution successful");
        } else {
            appendOutput("Error: " + result.getMessage() + "\n");
            statusLabel.setText("Execution failed");
        }
    }

    private void displayTableResult(QueryResult result) {
        SwingUtilities.invokeLater(() -> {
            // Clear table
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            try {
                List<Map<String, Object>> data = result.getData();
                if (data != null && !data.isEmpty()) {
                    // Get column names from first row
                    Map<String, Object> firstRow = data.get(0);
                    for (String column : firstRow.keySet()) {
                        tableModel.addColumn(column);
                    }

                    // Add data
                    for (Map<String, Object> row : data) {
                        Object[] rowData = new Object[firstRow.size()];
                        int i = 0;
                        for (String column : firstRow.keySet()) {
                            rowData[i++] = row.get(column);
                        }
                        tableModel.addRow(rowData);
                    }
                }
            } catch (Exception e) {
                // If table display fails, use text format
                appendOutput("Using text format for results:\n");
                try {
                    TableFormatter.displayResult(result);
                } catch (Exception ex) {
                    appendOutput("Failed to display results: " + ex.getMessage() + "\n");
                }
            }
        });
    }

    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }
}