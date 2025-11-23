package com.dbtool;

import com.dbtool.core.DatabaseManager;
import com.dbtool.core.SQLExecutor;
import com.dbtool.core.MetadataManager;
import com.dbtool.core.HistoryManager;
import com.dbtool.model.QueryResult;
import com.dbtool.util.TableFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class DBManagerGUI extends JFrame {
    private DatabaseManager dbManager;
    private SQLExecutor sqlExecutor;
    private MetadataManager metadataManager;
    private HistoryManager sqlHistoryManager;

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
            sqlHistoryManager = new HistoryManager(50);

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
        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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
        mainPanel.add(createToolbar(), BorderLayout.WEST);

        add(mainPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Event handlers
        setupEventHandlers();

        setVisible(true);
        appendOutput("Database Management Tool - GUI Started\n");
        appendOutput("Database connected successfully\n");
        appendOutput("Tip: Use toolbar buttons for quick actions\n");
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOrientation(JToolBar.VERTICAL);

        ToolbarCommand[] commands = {
                new ToolbarCommand("Show Databases", "SHOW_DATABASES"),
                new ToolbarCommand("Show Tables", "SHOW_TABLES"),
                new ToolbarCommand("Describe Table", "DESCRIBE_TABLE"),
                new ToolbarCommand("Database Info", "DATABASE_INFO"),
                new ToolbarCommand("Table Status", "TABLE_STATUS"),
                new ToolbarCommand("Connection Status", "STATUS"),
                new ToolbarCommand("Test Connection", "TEST_CONNECTION"),
                new ToolbarCommand("Get Tables", "GET_TABLES"),
                new ToolbarCommand("Validate SQL", "VALIDATE_SQL"),
                new ToolbarCommand("Prepared Query", "PREPARED_QUERY"),
                new ToolbarCommand("Batch Execute", "BATCH_EXECUTE")
        };

        for (ToolbarCommand cmd : commands) {
            JButton button = new JButton(cmd.getDisplayName());
            button.addActionListener(e -> executeToolbarCommand(cmd.getCommand()));
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
            executeSQLCommand(command);
            inputField.setText("");
        }
    }

    private void executeToolbarCommand(String command) {
        appendOutput("> [Toolbar] " + command + "\n");
        executeCommand(command);
    }

    private void executeSQLCommand(String sql) {
        appendOutput("> " + sql + "\n");

        // 添加到历史记录
        sqlHistoryManager.add(sql);

        executeCommand(sql);
    }

    private void executeCommand(String command) {
        try {
            if (command.equalsIgnoreCase("STATUS")) {
                handleStatusCommand();
            } else if (command.equalsIgnoreCase("TEST_CONNECTION")) {
                handleTestConnection();
            } else if (command.equalsIgnoreCase("SHOW_DATABASES")) {
                handleShowDatabases();
            } else if (command.equalsIgnoreCase("SHOW_TABLES")) {
                handleShowTables();
            } else if (command.equalsIgnoreCase("DESCRIBE_TABLE")) {
                handleDescribeTable();
            } else if (command.equalsIgnoreCase("DATABASE_INFO")) {
                handleDatabaseInfo();
            } else if (command.equalsIgnoreCase("TABLE_STATUS")) {
                handleTableStatus();
            } else if (command.equalsIgnoreCase("GET_TABLES")) {
                handleGetTables();
            } else if (command.equalsIgnoreCase("VALIDATE_SQL")) {
                handleValidateSQL();
            } else if (command.equalsIgnoreCase("PREPARED_QUERY")) {
                handlePreparedQuery();
            } else if (command.equalsIgnoreCase("BATCH_EXECUTE")) {
                handleBatchExecute();
            } else {
                // 执行SQL命令
                handleSQLExecution(command);
            }
        } catch (Exception e) {
            appendOutput("Error: " + e.getMessage() + "\n");
            statusLabel.setText("Execution failed");
        }
    }

    // ========== 新增的方法：使用 SQLExecutor 的新功能 ==========

    /**
     * 使用 SQLExecutor 的 getTables 方法获取表列表
     */
    private void handleGetTables() {
        appendOutput("Getting table list via SQLExecutor...\n");
        QueryResult result = sqlExecutor.getTables(dbManager.getConnection());
        displayResult(result);
        statusLabel.setText("Tables retrieved via SQLExecutor");
    }

    /**
     * 使用 SQLExecutor 的 validateSQL 方法验证 SQL 语法
     */
    private void handleValidateSQL() {
        String sql = JOptionPane.showInputDialog(this,
                "Enter SQL to validate:",
                "SQL Validation",
                JOptionPane.QUESTION_MESSAGE);

        if (sql != null && !sql.trim().isEmpty()) {
            appendOutput("Validating SQL: " + sql + "\n");
            QueryResult result = sqlExecutor.validateSQL(dbManager.getConnection(), sql);
            if (result.isSuccess()) {
                appendOutput("✓ SQL is valid: " + result.getMessage() + "\n");
                statusLabel.setText("SQL validation successful");
            } else {
                appendOutput("✗ SQL validation failed: " + result.getMessage() + "\n");
                statusLabel.setText("SQL validation failed");
            }
        }
    }

    /**
     * 使用 SQLExecutor 的 executePrepared 方法执行参数化查询
     */
    private void handlePreparedQuery() {
        // 创建参数输入对话框
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));

        JTextField sqlField = new JTextField("SELECT * FROM users WHERE age > ? AND name LIKE ?");
        JTextField ageField = new JTextField("18");
        JTextField nameField = new JTextField("%%");
        JCheckBox useParamsCheckbox = new JCheckBox("Use parameters", true);

        panel.add(new JLabel("SQL with parameters:"));
        panel.add(sqlField);
        panel.add(new JLabel("Age parameter:"));
        panel.add(ageField);
        panel.add(new JLabel("Name pattern:"));
        panel.add(nameField);
        panel.add(new JLabel(""));
        panel.add(useParamsCheckbox);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Prepared Query Parameters", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String sql = sqlField.getText().trim();
                List<Object> params = new ArrayList<>();

                if (useParamsCheckbox.isSelected()) {
                    if (!ageField.getText().trim().isEmpty()) {
                        params.add(Integer.parseInt(ageField.getText().trim()));
                    }
                    if (!nameField.getText().trim().isEmpty()) {
                        params.add(nameField.getText().trim());
                    }
                }

                appendOutput("Executing prepared query: " + sql + "\n");
                if (!params.isEmpty()) {
                    appendOutput("Parameters: " + params + "\n");
                }

                QueryResult queryResult = sqlExecutor.executePrepared(dbManager.getConnection(), sql, params);
                displayResult(queryResult);
                statusLabel.setText("Prepared query executed");

            } catch (NumberFormatException e) {
                appendOutput("Error: Invalid number format in parameters\n");
                statusLabel.setText("Parameter error");
            } catch (Exception e) {
                appendOutput("Error in prepared query: " + e.getMessage() + "\n");
                statusLabel.setText("Execution failed");
            }
        }
    }

    /**
     * 使用 SQLExecutor 的 executeMultiple 方法执行批量 SQL
     */
    private void handleBatchExecute() {
        JTextArea batchArea = new JTextArea(10, 50);
        batchArea.setText("SHOW TABLES;\nSELECT NOW();\nSELECT 1 + 1;");
        batchArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(batchArea);

        int result = JOptionPane.showConfirmDialog(this, scrollPane,
                "Enter Batch SQL (separate statements with semicolons)",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String batchSQL = batchArea.getText().trim();
            if (!batchSQL.isEmpty()) {
                appendOutput("Executing batch SQL...\n");
                appendOutput("Batch SQL:\n" + batchSQL + "\n");

                List<QueryResult> results = sqlExecutor.executeMultiple(dbManager.getConnection(), batchSQL);

                appendOutput("=== Batch Execution Results ===\n");
                for (int i = 0; i < results.size(); i++) {
                    QueryResult singleResult = results.get(i);
                    appendOutput("Statement " + (i + 1) + ": " +
                            (singleResult.isSuccess() ? "✓ " : "✗ ") +
                            singleResult.getMessage() + "\n");

                    // 如果有数据结果，显示表格
                    if (singleResult.isSuccess() && singleResult.getData() != null && !singleResult.getData().isEmpty()) {
                        appendOutput("Result data available for statement " + (i + 1) + "\n");
                        // 显示最后一个有数据的语句结果
                        if (i == results.size() - 1 ||
                                (i < results.size() - 1 && results.get(i + 1).getData() == null)) {
                            displayResult(singleResult);
                        }
                    }
                }
                statusLabel.setText("Batch execution completed - " + results.size() + " statements");
            }
        }
    }

    // ========== 原有的方法 ==========

    private void handleStatusCommand() {
        appendOutput("Connection Info: " + dbManager.getConnectionInfo() + "\n");
        boolean success = dbManager.testConnection();
        appendOutput(success ? "✓ Connection test successful\n" : "✗ Connection test failed\n");
        statusLabel.setText("Status checked");
    }

    private void handleTestConnection() {
        boolean success = dbManager.testConnection();
        appendOutput(success ? "✓ Connection test successful\n" : "✗ Connection test failed\n");
        statusLabel.setText("Test completed");
    }

    private void handleShowDatabases() {
        QueryResult result = metadataManager.getDatabases();
        displayResult(result);
    }

    private void handleShowTables() {
        QueryResult result = metadataManager.getTables();
        displayResult(result);
    }

    private void handleDescribeTable() {
        String tableName = JOptionPane.showInputDialog(this, "Enter table name:");
        if (tableName != null && !tableName.trim().isEmpty()) {
            QueryResult result = metadataManager.describeTable(tableName.trim());
            displayResult(result);
        }
    }

    private void handleDatabaseInfo() {
        QueryResult result = metadataManager.getDatabaseInfo();
        displayResult(result);
    }

    private void handleTableStatus() {
        QueryResult result = metadataManager.getTableStatus();
        displayResult(result);
    }

    private void handleSQLExecution(String sql) {
        QueryResult result = sqlExecutor.execute(dbManager.getConnection(), sql);
        displayResult(result);
    }

    private void displayResult(QueryResult result) {
        if (result.isSuccess()) {
            if (result.getData() != null && !result.getData().isEmpty()) {
                displayTableResult(result);
                appendOutput("✓ Query successful: " + result.getData().size() + " rows returned\n");
            } else {
                appendOutput("✓ Success: " + result.getMessage() + "\n");
            }
            statusLabel.setText("Execution successful");
        } else {
            appendOutput("✗ Error: " + result.getMessage() + "\n");
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
                            Object value = row.get(column);
                            rowData[i++] = (value != null) ? value : "NULL";
                        }
                        tableModel.addRow(rowData);
                    }

                    // Auto-resize columns after data is loaded
                    autoResizeTableColumns();
                }
            } catch (Exception e) {
                // If table display fails, use text format
                appendOutput("Using text format for results:\n");
                try {
                    String textResult = TableFormatter.formatAsText(result);
                    appendOutput(textResult + "\n");
                } catch (Exception ex) {
                    appendOutput("Failed to display results: " + ex.getMessage() + "\n");
                }
            }
        });
    }

    private void autoResizeTableColumns() {
        SwingUtilities.invokeLater(() -> {
            JTable table = (JTable) ((JViewport) ((JScrollPane)
                    ((JSplitPane) getContentPane().getComponent(0)).getRightComponent()).getComponent(0)).getView();

            for (int column = 0; column < table.getColumnCount(); column++) {
                TableColumn tableColumn = table.getColumnModel().getColumn(column);
                int preferredWidth = tableColumn.getMinWidth();
                int maxWidth = tableColumn.getMaxWidth();

                for (int row = 0; row < table.getRowCount(); row++) {
                    TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                    Component c = table.prepareRenderer(cellRenderer, row, column);
                    int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                    preferredWidth = Math.max(preferredWidth, width);

                    if (preferredWidth >= maxWidth) {
                        preferredWidth = maxWidth;
                        break;
                    }
                }

                tableColumn.setPreferredWidth(preferredWidth);
            }
        });
    }

    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    private static class ToolbarCommand {
        private final String displayName;
        private final String command;

        public ToolbarCommand(String displayName, String command) {
            this.displayName = displayName;
            this.command = command;
        }

        public String getDisplayName() { return displayName; }
        public String getCommand() { return command; }
    }

    public static void main(String[] args) {
        // 简化版本：直接启动GUI，不使用外观设置
        SwingUtilities.invokeLater(() -> {
            new DBManagerGUI();
        });
    }
}