package com.dbtool.core;

import com.dbtool.util.ConfigLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private Connection connection;
    private boolean connected = false;
    private String databaseUrl;
    private String username;
    private Properties connectionProps; // 添加这行声明

    public DatabaseManager() {
        this.connectionProps = new Properties(); // 现在这个变量已经声明了
        initialize(); // 添加初始化调用
    }

    private void initialize() {
        try {
            // 加载JDBC驱动
            Class.forName(ConfigLoader.get("db.driver"));
            System.out.println("✓ JDBC Driver loaded: " + ConfigLoader.get("db.driver"));
        } catch (ClassNotFoundException e) {
            System.err.println("✗ JDBC Driver not found: " + e.getMessage());
            System.err.println("Please make sure MySQL connector JAR is in lib/ directory");
        }
    }

    public boolean connect() {
        return connect(
                ConfigLoader.get("db.url"),
                ConfigLoader.get("db.username"),
                ConfigLoader.get("db.password")
        );
    }

    public boolean connect(String url, String user, String password) {
        try {
            System.out.print("Connecting to database... ");

            this.databaseUrl = url;
            this.username = user;

            // 使用connectionProps来设置连接属性
            connectionProps.setProperty("user", user);
            connectionProps.setProperty("password", password);
            connectionProps.setProperty("useSSL", "false");
            connectionProps.setProperty("serverTimezone", "UTC");
            connectionProps.setProperty("characterEncoding", "UTF-8");

            connection = DriverManager.getConnection(url, connectionProps);
            connected = true;

            System.out.println("✓ SUCCESS");
            System.out.println("  URL: " + url);
            System.out.println("  User: " + user);

            // 测试连接是否有效
            if (connection.isValid(5)) {
                System.out.println("✓ Connection validated");
            }

            return true;

        } catch (SQLException e) {
            connected = false;
            System.out.println("✗ FAILED");
            System.err.println("Connection error: " + e.getMessage());
            System.err.println("Please check:");
            System.err.println("  1. Database server is running");
            System.err.println("  2. URL, username and password are correct");
            System.err.println("  3. MySQL connector JAR is available");
            return false;
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connected = false;
                System.out.println("✓ Disconnected from database");
            } catch (SQLException e) {
                System.err.println("Error disconnecting: " + e.getMessage());
            }
        }
    }

    public Connection getConnection() {
        if (!connected) {
            System.err.println("No active database connection");
            return null;
        }
        return connection;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getConnectionInfo() {
        if (!connected) {
            return "Not connected to any database";
        }
        return String.format("Connected to: %s as %s", databaseUrl, username);
    }

    public boolean testConnection() {
        if (!connected) {
            return false;
        }

        try {
            return connection.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    // 在DatabaseManager类中添加连接池方法
    public boolean connectWithPool() {
        try {
            // 简单的连接池实现
            String url = ConfigLoader.get("db.url");
            String user = ConfigLoader.get("db.username");
            String password = ConfigLoader.get("db.password");

            // 设置连接参数 - 使用已经声明的connectionProps
            connectionProps.setProperty("user", user);
            connectionProps.setProperty("password", password);
            connectionProps.setProperty("useSSL", "false");
            connectionProps.setProperty("serverTimezone", "UTC");
            connectionProps.setProperty("characterEncoding", "UTF-8");

            connection = DriverManager.getConnection(url, connectionProps);
            connected = true;

            return true;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    // 添加一个方法来获取或设置连接属性
    public Properties getConnectionProperties() {
        return connectionProps;
    }

    public void setConnectionProperty(String key, String value) {
        connectionProps.setProperty(key, value);
    }
}