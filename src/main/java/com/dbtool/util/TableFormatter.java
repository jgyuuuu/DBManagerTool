package com.dbtool.util;

import com.dbtool.model.QueryResult;

import java.util.List;
import java.util.Map;

public class TableFormatter {

    public static void displayResult(QueryResult result) {
        if (!result.isSuccess()) {
            System.err.println("❌ " + result.getMessage());
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
            System.out.println("ℹ️  No data found.");
            System.out.println("⏱️  " + result.getMessage());
            return;
        }

        int[] columnWidths = calculateColumnWidths(data, columnNames);

        printHorizontalLine(columnWidths);
        printHeader(columnNames, columnWidths);
        printHorizontalLine(columnWidths);
        printDataRows(data, columnNames, columnWidths);
        printHorizontalLine(columnWidths);

        System.out.println("📊 " + result.getRowCount() + " row(s) returned");
        System.out.println("⏱️  " + result.getMessage());
    }

    private static void displayUpdateResult(QueryResult result) {
        System.out.println("✅ " + result.getMessage());
        System.out.println("📝 " + result.getRowCount() + " row(s) affected");
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

    private static void printHorizontalLine(int[] widths) {
        StringBuilder line = new StringBuilder("+");
        for (int width : widths) {
            line.append(repeatChar('-', width + 2)).append("+");
        }
        System.out.println(line);
    }

    private static void printHeader(List<String> columnNames, int[] widths) {
        StringBuilder row = new StringBuilder("|");
        for (int i = 0; i < columnNames.size(); i++) {
            row.append(" ").append(padRight(columnNames.get(i), widths[i])).append(" |");
        }
        System.out.println(row);
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

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
};