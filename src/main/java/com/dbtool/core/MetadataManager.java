package com.dbtool.core;

import com.dbtool.model.QueryResult;

import java.sql.*;
import java.util.*;

public class MetadataManager {
    private Connection connection;

    public MetadataManager(Connection connection) {
        this.connection = connection;
    }

    public QueryResult getDatabases() {
        try {
            String sql = "SHOW DATABASES";
            return executeMetadataQuery(sql, "Database list");
        } catch (SQLException e) {
            return QueryResult.error("Failed to get databases: " + e.getMessage());
        }
    }

    public QueryResult getTables() {
        try {
            String sql = "SHOW TABLES";
            return executeMetadataQuery(sql, "Table list");
        } catch (SQLException e) {
            return QueryResult.error("Failed to get tables: " + e.getMessage());
        }
    }

    public QueryResult getTables(String databaseName) {
        try {
            String sql = "SHOW TABLES FROM " + databaseName;
            return executeMetadataQuery(sql, "Tables in " + databaseName);
        } catch (SQLException e) {
            return QueryResult.error("Failed to get tables: " + e.getMessage());
        }
    }

    public QueryResult describeTable(String tableName) {
        try {
            String sql = "DESCRIBE " + tableName;
            return executeMetadataQuery(sql, "Table structure: " + tableName);
        } catch (SQLException e) {
            return QueryResult.error("Failed to describe table: " + e.getMessage());
        }
    }

    public QueryResult getTableInfo(String tableName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, "%");
            return resultSetToQueryResult(columns, "Columns in " + tableName);
        } catch (SQLException e) {
            return QueryResult.error("Failed to get table info: " + e.getMessage());
        }
    }

    public QueryResult getTableStatus() {
        try {
            String sql = "SHOW TABLE STATUS";
            return executeMetadataQuery(sql, "Table status");
        } catch (SQLException e) {
            return QueryResult.error("Failed to get table status: " + e.getMessage());
        }
    }

    public QueryResult getDatabaseInfo() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();

            List<Map<String, Object>> data = new ArrayList<>();
            Map<String, Object> row = new LinkedHashMap<>();

            row.put("Database Product", metaData.getDatabaseProductName());
            row.put("Database Version", metaData.getDatabaseProductVersion());
            row.put("Driver Name", metaData.getDriverName());
            row.put("Driver Version", metaData.getDriverVersion());
            row.put("JDBC Version", metaData.getJDBCMajorVersion() + "." + metaData.getJDBCMinorVersion());
            row.put("URL", metaData.getURL());
            row.put("User", metaData.getUserName());
            row.put("Read Only", metaData.isReadOnly());
            row.put("Supports Transactions", metaData.supportsTransactions());

            data.add(row);

            List<String> columnNames = new ArrayList<>(row.keySet());
            return QueryResult.success("Database information", data, columnNames, 1, 0);

        } catch (SQLException e) {
            return QueryResult.error("Failed to get database info: " + e.getMessage());
        }
    }

    private QueryResult executeMetadataQuery(String sql, String message) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return resultSetToQueryResult(rs, message);
        }
    }

    private QueryResult resultSetToQueryResult(ResultSet rs, String message) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        List<String> columnNames = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
            columnNames.add(metaData.getColumnLabel(i));
        }

        List<Map<String, Object>> data = new ArrayList<>();
        int rowCount = 0;

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = columnNames.get(i - 1);
                Object value = rs.getObject(i);
                row.put(columnName, value);
            }
            data.add(row);
            rowCount++;
        }

        return QueryResult.success(message, data, columnNames, rowCount, 0);
    }
}