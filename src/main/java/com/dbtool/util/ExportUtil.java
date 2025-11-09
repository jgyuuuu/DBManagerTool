package com.dbtool.util;

import com.dbtool.model.QueryResult;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExportUtil {

    public static boolean exportToCSV(QueryResult result, String filename) {
        if (!result.isSuccess() || !result.isQueryResult()) {
            System.err.println("Cannot export: " + result.getMessage());
            return false;
        }

        if (filename == null || filename.trim().isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            filename = "export_" + timestamp + ".csv";
        }

        if (!filename.toLowerCase().endsWith(".csv")) {
            filename += ".csv";
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            List<String> columnNames = result.getColumnNames();
            List<Map<String, Object>> data = result.getData();

            // 写入列头
            writer.println(String.join(",", columnNames));

            // 写入数据
            for (Map<String, Object> row : data) {
                StringBuilder csvLine = new StringBuilder();
                for (String columnName : columnNames) {
                    Object value = row.get(columnName);
                    String strValue = (value != null) ? escapeCsv(value.toString()) : "";
                    csvLine.append(strValue).append(",");
                }
                // 移除最后一个逗号
                if (csvLine.length() > 0) {
                    csvLine.setLength(csvLine.length() - 1);
                }
                writer.println(csvLine);
            }

            System.out.println("✅ Data exported to: " + filename);
            System.out.println("📊 " + data.size() + " rows exported");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Export failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean exportToText(QueryResult result, String filename) {
        if (!result.isSuccess() || !result.isQueryResult()) {
            System.err.println("Cannot export: " + result.getMessage());
            return false;
        }

        if (filename == null || filename.trim().isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            filename = "export_" + timestamp + ".txt";
        }

        if (!filename.toLowerCase().endsWith(".txt")) {
            filename += ".txt";
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            List<String> columnNames = result.getColumnNames();
            List<Map<String, Object>> data = result.getData();

            // 写入标题和信息
            writer.println("Database Export");
            writer.println("Generated: " + new Date());
            writer.println("Total Rows: " + data.size());
            writer.println();

            // 计算列宽（用于文本对齐）
            int[] columnWidths = calculateColumnWidths(data, columnNames);

            // 写入表头
            writer.println(createTextTableLine(columnWidths));
            writer.println(createTextHeaderLine(columnNames, columnWidths));
            writer.println(createTextTableLine(columnWidths));

            // 写入数据
            for (Map<String, Object> row : data) {
                writer.println(createTextDataLine(row, columnNames, columnWidths));
            }

            writer.println(createTextTableLine(columnWidths));

            System.out.println("✅ Data exported to: " + filename);
            System.out.println("📊 " + data.size() + " rows exported");
            return true;

        } catch (IOException e) {
            System.err.println("❌ Export failed: " + e.getMessage());
            return false;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static int[] calculateColumnWidths(List<Map<String, Object>> data, List<String> columnNames) {
        int[] widths = new int[columnNames.size()];

        for (int i = 0; i < columnNames.size(); i++) {
            widths[i] = Math.max(columnNames.get(i).length(), 4);
        }

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

    // 修复：使用自定义repeat方法替代String.repeat()
    private static String createTextTableLine(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append(repeatChar('-', width + 2)).append("+");
        }
        return line.toString();
    }

    // 修复：使用正确的边框字符
    private static String createTextHeaderLine(List<String> columnNames, int[] widths) {
        StringBuilder row = new StringBuilder("|");  // 使用 | 而不是 !
        for (int i = 0; i < columnNames.size(); i++) {
            row.append(" ").append(padRight(columnNames.get(i), widths[i])).append(" |");
        }
        return row.toString();
    }

    private static String createTextDataLine(Map<String, Object> rowData, List<String> columnNames, int[] widths) {
        StringBuilder row = new StringBuilder("|");
        for (int i = 0; i < columnNames.size(); i++) {
            String colName = columnNames.get(i);
            Object value = rowData.get(colName);
            String strValue = (value != null) ? value.toString() : "NULL";
            row.append(" ").append(padRight(strValue, widths[i])).append(" |");
        }
        return row.toString();
    }

    private static String padRight(String s, int length) {
        if (s == null) s = "NULL";
        if (s.length() >= length) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }

    // 新增：兼容Java 8的字符重复方法
    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}