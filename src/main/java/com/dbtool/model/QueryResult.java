package com.dbtool.model;

import java.util.List;
import java.util.Map;

public class QueryResult {
    private boolean success;
    private String message;
    private List<Map<String, Object>> data;
    private List<String> columnNames;
    private int rowCount;
    private long executionTime;

    // 成功构造方法 - 用于查询结果
    public QueryResult(boolean success, String message, List<Map<String, Object>> data,
                       List<String> columnNames, int rowCount, long executionTime) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.columnNames = columnNames;
        this.rowCount = rowCount;
        this.executionTime = executionTime;
    }

    // 简单构造方法 - 用于更新操作
    public QueryResult(boolean success, String message, int rowCount, long executionTime) {
        this(success, message, null, null, rowCount, executionTime);
    }

    // 错误构造方法
    public QueryResult(boolean success, String message) {
        this(success, message, null, null, 0, 0);
    }

    // 静态工厂方法
    public static QueryResult success(String message, List<Map<String, Object>> data,
                                      List<String> columnNames, int rowCount, long executionTime) {
        return new QueryResult(true, message, data, columnNames, rowCount, executionTime);
    }

    public static QueryResult updateSuccess(String message, int rowCount, long executionTime) {
        return new QueryResult(true, message, rowCount, executionTime);
    }

    public static QueryResult error(String message) {
        return new QueryResult(false, message);
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Map<String, Object>> getData() { return data; }
    public List<String> getColumnNames() { return columnNames; }
    public int getRowCount() { return rowCount; }
    public long getExecutionTime() { return executionTime; }
    public boolean isQueryResult() { return data != null; }
}