package com.dbtool.util;

import com.dbtool.model.QueryResult;

import java.util.List;
import java.util.Map;

public class TableFormatter {

    /**
     * 将查询结果格式化为文本表格
     */
    public static String formatAsText(QueryResult result) {
        if (!result.isSuccess()) {
            return "Error: " + result.getMessage();
        }

        if (result.isQueryResult()) {
            return formatQueryResultAsText(result);
        } else {
            return formatUpdateResultAsText(result);
        }
    }

    /**
     * 替代方法名，功能与 formatAsText 相同
     */
    public static String formatResultAsText(QueryResult result) {
        return formatAsText(result);
    }

    private static String formatQueryResultAsText(QueryResult result) {
        StringBuilder sb = new StringBuilder();
        List<Map<String, Object>> data = result.getData();
        List<String> columnNames = result.getColumnNames();

        if (data == null || data.isEmpty()) {
            sb.append("No data found.\n");
            sb.append(result.getMessage());
            return sb.toString();
        }

        int[] columnWidths = calculateColumnWidths(data, columnNames);

        sb.append(createHorizontalLine(columnWidths)).append("\n");
        sb.append(createHeaderRow(columnNames, columnWidths)).append("\n");
        sb.append(createHorizontalLine(columnWidths)).append("\n");
        sb.append(createDataRows(data, columnNames, columnWidths));
        sb.append(createHorizontalLine(columnWidths)).append("\n");

        sb.append(result.getRowCount()).append(" row(s) returned\n");
        sb.append(result.getMessage());

        return sb.toString();
    }

    private static String formatUpdateResultAsText(QueryResult result) {
        return result.getMessage() + "\n" + result.getRowCount() + " row(s) affected";
    }

    /**
     * 原有的显示方法（控制台输出）
     */
    public static void displayResult(QueryResult result) {
        if (!result.isSuccess()) {
            System.err.println(result.getMessage());
            return;
        }

        if (result.isQueryResult()) {
            displayQueryResult(result);
        } else {
            displayUpdateResult(result);
        }
    }

    private static void displayQueryResult(QueryResult result) {
        List<Map<String, Object>> data = result.getData();
        List<String> columnNames = result.getColumnNames();

        if (data == null || data.isEmpty()) {
            System.out.println("No data found.");
            System.out.println(result.getMessage());
            return;
        }

        int[] columnWidths = calculateColumnWidths(data, columnNames);

        printHorizontalLine(columnWidths);
        printHeader(columnNames, columnWidths);
        printHorizontalLine(columnWidths);
        printDataRows(data, columnNames, columnWidths);
        printHorizontalLine(columnWidths);

        System.out.println(result.getRowCount() + " row(s) returned");
        System.out.println(result.getMessage());
    }

    private static void displayUpdateResult(QueryResult result) {
        System.out.println(result.getMessage());
        System.out.println(result.getRowCount() + " row(s) affected");
    }

    /**
     * 计算每列的最大宽度
     */
    private static int[] calculateColumnWidths(List<Map<String, Object>> data, List<String> columnNames) {
        int[] widths = new int[columnNames.size()];

        // 初始化列宽为列名的长度
        for (int i = 0; i < columnNames.size(); i++) {
            widths[i] = Math.max(columnNames.get(i).length(), 4);
        }

        // 根据数据内容调整列宽
        for (Map<String, Object> row : data) {
            for (int i = 0; i < columnNames.size(); i++) {
                String colName = columnNames.get(i);
                Object value = row.get(colName);
                String strValue = (value != null) ? value.toString() : "NULL";
                widths[i] = Math.max(widths[i], strValue.length());
            }
        }

        return widths;
    }

    /**
     * 创建水平分隔线
     */
    private static String createHorizontalLine(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append(repeatChar('-', width + 2)).append("+");
        }
        return line.toString();
    }

    /**
     * 创建表头行
     */
    private static String createHeaderRow(List<String> columnNames, int[] widths) {
        StringBuilder row = new StringBuilder("|");
        for (int i = 0; i < columnNames.size(); i++) {
            row.append(" ").append(padRight(columnNames.get(i), widths[i])).append(" |");
        }
        return row.toString();
    }

    /**
     * 创建数据行
     */
    private static String createDataRows(List<Map<String, Object>> data,
                                         List<String> columnNames, int[] widths) {
        StringBuilder allRows = new StringBuilder();
        for (Map<String, Object> rowData : data) {
            StringBuilder row = new StringBuilder("|");
            for (int i = 0; i < columnNames.size(); i++) {
                String colName = columnNames.get(i);
                Object value = rowData.get(colName);
                String strValue = (value != null) ? value.toString() : "NULL";
                row.append(" ").append(padRight(strValue, widths[i])).append(" |");
            }
            allRows.append(row.toString()).append("\n");
        }
        return allRows.toString();
    }

    /**
     * 原有的控制台输出方法
     */
    private static void printHorizontalLine(int[] widths) {
        System.out.println(createHorizontalLine(widths));
    }

    private static void printHeader(List<String> columnNames, int[] widths) {
        System.out.println(createHeaderRow(columnNames, widths));
    }

    private static void printDataRows(List<Map<String, Object>> data,
                                      List<String> columnNames, int[] widths) {
        for (Map<String, Object> rowData : data) {
            StringBuilder row = new StringBuilder("|");
            for (int i = 0; i < columnNames.size(); i++) {
                String colName = columnNames.get(i);
                Object value = rowData.get(colName);
                String strValue = (value != null) ? value.toString() : "NULL";
                row.append(" ").append(padRight(strValue, widths[i])).append(" |");
            }
            System.out.println(row);
        }
    }

    /**
     * 辅助方法：右填充字符串
     */
    private static String padRight(String s, int length) {
        if (s == null) {
            s = "NULL";
        }
        if (s.length() >= length) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * 辅助方法：重复字符
     */
    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}