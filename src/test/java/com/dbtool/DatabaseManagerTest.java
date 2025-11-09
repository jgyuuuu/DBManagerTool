package com.dbtool;

import com.dbtool.core.DatabaseManager;  // 正确的包路径
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        dbManager = new DatabaseManager();
    }

    @AfterEach
    void tearDown() {
        if (dbManager != null) {
            dbManager.disconnect();
        }
    }

    @Test
    @DisplayName("Test disconnect")
    void testDisconnect() {
        assertDoesNotThrow(() -> dbManager.disconnect());
    }

    @Test
    @DisplayName("Test connection status")
    void testIsConnected() {
        assertFalse(dbManager.isConnected());
    }

    @Test
    @DisplayName("Test get connection info")
    void testGetConnectionInfo() {
        String info = dbManager.getConnectionInfo();
        assertNotNull(info);
    }
}