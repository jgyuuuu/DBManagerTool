package com.dbtool.core;

import java.io.*;
import java.util.*;

public class HistoryManager {
    private static final int MAX_HISTORY_SIZE = 100;
    private static final String HISTORY_FILE = ".dbmanager_history";
    private LinkedList<String> history;
    private int currentIndex;

    public HistoryManager() {
        this.history = new LinkedList<>();
        this.currentIndex = -1;
        loadHistory();
    }

    public void addToHistory(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        String trimmed = command.trim();

        // 移除重复的历史记录
        history.remove(trimmed);

        // 添加到开头
        history.addFirst(trimmed);

        // 限制历史记录大小
        if (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }

        currentIndex = -1;
        saveHistory();
    }

    public String getPrevious() {
        if (history.isEmpty()) {
            return null;
        }

        if (currentIndex < history.size() - 1) {
            currentIndex++;
        }

        return history.get(currentIndex);
    }

    public String getNext() {
        if (history.isEmpty() || currentIndex < 0) {
            return null;
        }

        if (currentIndex > 0) {
            currentIndex--;
            return history.get(currentIndex);
        } else {
            currentIndex = -1;
            return "";
        }
    }

    public List<String> getHistory(int count) {
        if (count <= 0 || count > history.size()) {
            count = history.size();
        }

        return new ArrayList<>(history.subList(0, count));
    }

    public void clearHistory() {
        history.clear();
        currentIndex = -1;
        saveHistory();
    }

    public void showHistory(int count) {
        List<String> recentHistory = getHistory(count);

        if (recentHistory.isEmpty()) {
            System.out.println("No command history found.");
            return;
        }

        System.out.println("Command History (latest " + recentHistory.size() + " commands):");
        for (int i = 0; i < recentHistory.size(); i++) {
            System.out.printf("%3d: %s%n", i + 1, recentHistory.get(i));
        }
    }

    private void loadHistory() {
        File historyFile = new File(HISTORY_FILE);
        if (!historyFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    history.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load command history: " + e.getMessage());
        }
    }

    private void saveHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            for (String command : history) {
                writer.println(command);
            }
        } catch (IOException e) {
            System.err.println("Failed to save command history: " + e.getMessage());
        }
    }
}