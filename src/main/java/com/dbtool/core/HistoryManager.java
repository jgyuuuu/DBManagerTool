package com.dbtool.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class HistoryManager {
    private final List<String> history = new ArrayList<>();
    private final int maxSize;

    public HistoryManager() {
        this(100); // Default: keep 100 records
    }

    public HistoryManager(int maxSize) {
        this.maxSize = Math.max(10, maxSize); // Keep at least 10 records
    }

    public void add(String item) {
        if (item != null && !item.trim().isEmpty()) {
            String trimmedItem = item.trim();

            // Avoid adding duplicate adjacent records
            if (!history.isEmpty() && history.get(history.size() - 1).equals(trimmedItem)) {
                return;
            }

            // Remove existing identical records to avoid duplicates
            history.remove(trimmedItem);

            history.add(trimmedItem);

            // Limit history size
            while (history.size() > maxSize) {
                history.remove(0); // Remove oldest record
            }
        }
    }

    public List<String> getAll() {
        List<String> result = new ArrayList<>(history);
        Collections.reverse(result); // Show newest records first
        return result;
    }

    public List<String> getRecent(int count) {
        int actualCount = Math.min(count, history.size());
        List<String> result = new ArrayList<>();

        for (int i = history.size() - 1; i >= history.size() - actualCount; i--) {
            result.add(history.get(i));
        }

        return result;
    }

    public List<String> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAll();
        }

        String searchTerm = keyword.toLowerCase().trim();
        return history.stream()
                .filter(item -> item.toLowerCase().contains(searchTerm))
                .sorted((a, b) -> Integer.compare(
                        history.indexOf(b), history.indexOf(a))) // Sort by time descending
                .collect(Collectors.toList());
    }

    public void addToHistory(String item) {
        add(item);
    }

    public void showHistory(int count) {
        List<String> recent = getRecent(count);
        for (int i = 0; i < recent.size(); i++) {
            System.out.println((i + 1) + ". " + recent.get(i));
        }
    }

    public void clearHistory() {
        clear();
    }

    public void clear() {
        history.clear();
    }

    public int size() {
        return history.size();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

}