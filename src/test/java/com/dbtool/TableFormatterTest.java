package com.dbtool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.sql.*;
import java.util.List;


public class TableFormatterTest {

    @Test
    void testDisplaySuccessResult() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("name", "John");
        data.add(row);

        List<String> columnNames = Arrays.asList("id", "name");
        QueryResult result = new QueryResult(true, "Test success", data, columnNames, 1, 100);

        // 这个方法主要测试不抛出异常
        assertDoesNotThrow(() -> TableFormatter.displayResult(result));
    }

    @Test
    void testDisplayErrorResult() {
        QueryResult result = QueryResult.error("Test error");
        assertDoesNotThrow(() -> TableFormatter.displayResult(result));
    }
}