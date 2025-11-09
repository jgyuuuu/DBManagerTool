package com.dbtool.util;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static Properties properties = new Properties();
    private static boolean loaded = false;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input != null) {
                properties.load(input);
                loaded = true;
                System.out.println("✓ Configuration loaded from resources");
            } else {
                setDefaultConfig();
                System.out.println("⚠ Using default configuration");
            }
        } catch (Exception e) {
            setDefaultConfig();
            System.err.println("⚠ Config file not found, using defaults: " + e.getMessage());
        }
    }

    private static void setDefaultConfig() {
        properties.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/test");
        properties.setProperty("db.username", "root");
        properties.setProperty("db.password", "");
        properties.setProperty("app.max.rows", "1000");
        properties.setProperty("display.color", "true");
        loaded = true;
    }

    public static String get(String key) {
        if (!loaded) {
            loadConfig();
        }
        return properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        if (!loaded) {
            loadConfig();
        }
        return properties.getProperty(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }

    public static void printConfig() {
        System.out.println("=== Current Configuration ===");
        properties.forEach((key, value) -> {
            if (!key.toString().contains("password")) {
                System.out.printf("%-20s: %s%n", key, value);
            } else {
                System.out.printf("%-20s: %s%n", key, "******");
            }
        });
        System.out.println("=============================");
    }
}