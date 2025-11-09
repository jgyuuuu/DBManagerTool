package com.dbtool;

import com.dbtool.util.TableFormatter;
import com.dbtool.model.QueryResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TableFormatterTest {

    @Test
    @DisplayName("Test TableFormatter class exists")
    void testTableFormatterClass() {
        assertNotNull(TableFormatter.class);
    }

    @Test
    @DisplayName("Test displayResult with successful query")
    void testDisplayResultWithQuery() {
        // 使用正确的构造函数创建查询结果
        List<String> columnNames = Arrays.asList("ID", "Name", "Age");
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> row1 = new HashMap<>();
        row1.put("ID", 1);
        row1.put("Name", "John");
        row1.put("Age", 25);
        data.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("ID", 2);
        row2.put("Name", "Jane");
        row2.put("Age", 30);
        data.add(row2);

        QueryResult result = new QueryResult(true, "Query executed successfully",
                data, columnNames, 2, 150L);

        // 测试不会抛出异常
        assertDoesNotThrow(() -> {
            TableFormatter.displayResult(result);
        });
    }

    @Test
    @DisplayName("Test displayResult with update result")
    void testDisplayResultWithUpdate() {
        // 使用更新操作的构造函数
        QueryResult result = new QueryResult(true, "Update completed successfully", 5, 50L);

        // 测试不会抛出异常
        assertDoesNotThrow(() -> {
            TableFormatter.displayResult(result);
        });
    }

    @Test
    @DisplayName("Test displayResult with failed query")
    void testDisplayResultWithFailure() {
        // 使用错误构造函数
        QueryResult result = new QueryResult(false, "SQL syntax error");

        // 测试不会抛出异常
        assertDoesNotThrow(() -> {
            TableFormatter.displayResult(result);
        });
    }

    @Test
    @DisplayName("Test displayResult with empty data")
    void testDisplayResultWithEmptyData() {
        // 使用静态工厂方法创建空数据结果
        List<String> columnNames = Arrays.asList("ID", "Name");
        List<Map<String, Object>> data = new ArrayList<>();

        QueryResult result = QueryResult.success("No data found", data, columnNames, 0, 100L);

        // 测试不会抛出异常
        assertDoesNotThrow(() -> {
            TableFormatter.displayResult(result);
        });
    }

    @Test
    @DisplayName("Test displayResult using factory methods")
    void testDisplayResultWithFactoryMethods() {
        // 测试静态工厂方法
        List<String> columnNames = Arrays.asList("ID", "Name");
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("ID", 1);
        row.put("Name", "Test");
        data.add(row);

        QueryResult successResult = QueryResult.success("Success", data, columnNames, 1, 200L);
        QueryResult updateResult = QueryResult.updateSuccess("Updated", 3, 75L);
        QueryResult errorResult = QueryResult.error("Error occurred");

        // 测试所有情况都不会抛出异常
        assertDoesNotThrow(() -> {
            TableFormatter.displayResult(successResult);
            TableFormatter.displayResult(updateResult);
            TableFormatter.displayResult(errorResult);
        });
    }
}