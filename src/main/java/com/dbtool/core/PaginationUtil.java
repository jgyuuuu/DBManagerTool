package com.dbtool.core;

import com.dbtool.model.QueryResult;
import com.dbtool.util.TableFormatter;

import java.util.List;
import java.util.Map;

public class PaginationUtil {
    private static final int DEFAULT_PAGE_SIZE = 10;

    public static void displayPaginatedResult(QueryResult result, int page, int pageSize) {
        if (!result.isSuccess()) {
            System.err.println(result.getMessage());
            return;
        }

        if (!result.isQueryResult()) {
            // 对于更新操作，直接显示结果
            TableFormatter.displayResult(result);
            return;
        }

        List<Map<String, Object>> data = result.getData();
        List<String> columnNames = result.getColumnNames();

        if (data == null || data.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        int totalRows = data.size();
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRows);

        List<Map<String, Object>> pageData = data.subList(startIndex, endIndex);

        // 创建分页后的结果对象
        QueryResult pageResult = new QueryResult(
                true,
                String.format("Page %d of %d (%d-%d of %d rows)",
                        page, totalPages, startIndex + 1, endIndex, totalRows),
                pageData,
                columnNames,
                pageData.size(),
                result.getExecutionTime()
        );

        // 显示当前页的数据
        TableFormatter.displayResult(pageResult);

        // 显示分页信息
        if (totalPages > 1) {
            System.out.println("\nNavigation: ");
            if (page > 1) {
                System.out.print("  [P]revious page  ");
            }
            if (page < totalPages) {
                System.out.print("  [N]ext page  ");
            }
            System.out.print("  [Q]uit paging");
            System.out.println();
        }
    }

    public static boolean isPaginationCommand(String input) {
        if (input == null) return false;
        String lower = input.trim().toLowerCase();
        return lower.equals("p") || lower.equals("n") || lower.equals("q");
    }
}