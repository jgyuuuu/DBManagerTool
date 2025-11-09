package com.dbtool.core;

import com.dbtool.model.QueryResult;

import java.sql.*;
import java.util.*;

public class SQLExecutor {

    public QueryResult execute(Connection connection, String sql) {
        if (connection == null) {
            return QueryResult.error("No database connection available");
        }

        long startTime = System.currentTimeMillis();

        try {
            // 去除SQL前后的空格并转换为小写用于判断
            String trimmedSQL = sql.trim();
            String lowerSQL = trimmedSQL.toLowerCase();

            // 判断SQL类型
            if (isQuerySQL(lowerSQL)) {
                return executeQuery(connection, trimmedSQL, startTime);
            } else {
                return executeUpdate(connection, trimmedSQL, startTime);
            }

        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            return QueryResult.error("SQL Error: " + e.getMessage() + " (took " + (endTime - startTime) + "ms)");
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            return QueryResult.error("Unexpected error: " + e.getMessage() + " (took " + (endTime - startTime) + "ms)");
        }
    }

    private boolean isQuerySQL(String sql) {
        return sql.startsWith("select") ||
                sql.startsWith("show") ||
                sql.startsWith("describe") ||
                sql.startsWith("explain") ||
                sql.startsWith("with");
    }

    private QueryResult executeQuery(Connection connection, String sql, long startTime) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 获取列名
            List<String> columnNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnLabel(i));
            }

            // 转换结果数据
            List<Map<String, Object>> data = new ArrayList<>();
            int rowCount = 0;

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>(); // 保持列顺序
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = columnNames.get(i - 1);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                data.add(row);
                rowCount++;
            }

            long endTime = System.currentTimeMillis();
            String message = String.format("Query executed successfully (%d ms)", (endTime - startTime));

            return QueryResult.success(message, data, columnNames, rowCount, (endTime - startTime));
        }
    }

    private QueryResult executeUpdate(Connection connection, String sql, long startTime) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            int affectedRows = stmt.executeUpdate(sql);

            long endTime = System.currentTimeMillis();
            String message = String.format("Update completed (%d ms)", (endTime - startTime));

            return QueryResult.updateSuccess(message, affectedRows, (endTime - startTime));
        }
    }

    // 获取数据库元数据的方法（为明天准备）
    public QueryResult getTables(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            return resultSetToQueryResult(tables, "Tables list");
        } catch (SQLException e) {
            return QueryResult.error("Failed to get tables: " + e.getMessage());
        }
    }

    // 工具方法：将ResultSet转换为QueryResult
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

    // 在SQLExecutor类中添加这些方法：

    /**
     * 执行预处理语句（防止SQL注入）
     */
    public QueryResult executePrepared(Connection connection, String sql, List<Object> parameters) {
        if (connection == null) {
            return QueryResult.error("No database connection available");
        }

        long startTime = System.currentTimeMillis();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // 设置参数
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            boolean isResultSet = pstmt.execute();

            if (isResultSet) {
                try (ResultSet rs = pstmt.getResultSet()) {
                    return handleQueryResult(rs, startTime);
                }
            } else {
                int affectedRows = pstmt.getUpdateCount();
                return handleUpdateResult(affectedRows, startTime);
            }

        } catch (SQLException e) {
            long endTime = System.currentTimeMillis();
            return QueryResult.error("SQL Error: " + e.getMessage() + " (took " + (endTime - startTime) + "ms)");
        }
    }

    /**
     * 执行多条SQL语句（用分号分隔）
     */
    public List<QueryResult> executeMultiple(Connection connection, String sqlBatch) {
        List<QueryResult> results = new ArrayList<>();
        String[] statements = sqlBatch.split(";");

        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                results.add(execute(connection, trimmed));
            }
        }

        return results;
    }

    /**
     * 检查SQL语法（不实际执行）
     */
    public QueryResult validateSQL(Connection connection, String sql) {
        try {
            // 尝试准备语句来验证语法
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                return QueryResult.success("SQL syntax is valid", null, null, 0, 0);
            }
        } catch (SQLException e) {
            return QueryResult.error("SQL syntax error: " + e.getMessage());
        }
    }

    // 更新原有的handleQueryResult和handleUpdateResult方法，添加执行时间参数
    private QueryResult handleQueryResult(ResultSet rs, long startTime) throws SQLException {
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

        long endTime = System.currentTimeMillis();
        String message = String.format("Query executed successfully (%d ms)", (endTime - startTime));

        return QueryResult.success(message, data, columnNames, rowCount, (endTime - startTime));
    }

    private QueryResult handleUpdateResult(int affectedRows, long startTime) {
        long endTime = System.currentTimeMillis();
        String message = String.format("Update completed (%d ms)", (endTime - startTime));
        return QueryResult.updateSuccess(message, affectedRows, (endTime - startTime));
    }
}