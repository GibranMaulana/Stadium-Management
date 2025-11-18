package org.openjfx.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for database connection management
 * Uses environment variables from .env file for configuration
 */
public class DatabaseUtil {
    
    // Environment variables loaded from .env file
    private static final Map<String, String> ENV_VARS = new HashMap<>();
    
    static {
        // Try to load .env file
        try {
            loadEnvFile();
            System.out.println("✓ Environment variables loaded from .env file");
        } catch (IOException e) {
            System.err.println("⚠ Warning: Could not load .env file: " + e.getMessage());
            System.err.println("⚠ Make sure to create .env file with database credentials");
        }
    }
    
    /**
     * Load environment variables from .env file
     */
    private static void loadEnvFile() throws IOException {
        Path envPath = Paths.get(".env");
        
        if (!Files.exists(envPath)) {
            // Try parent directory
            envPath = Paths.get("../.env");
            if (!Files.exists(envPath)) {
                throw new IOException(".env file not found in current or parent directory");
            }
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse KEY=VALUE
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    ENV_VARS.put(key, value);
                }
            }
        }
    }
    
    /**
     * Get environment variable with fallback default
     */
    private static String getEnv(String key, String defaultValue) {
        String value = ENV_VARS.get(key);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }
        return defaultValue;
    }
    
    // Database configuration from environment variables with fallback defaults
    private static final String DB_HOST = getEnv("DB_HOST", "localhost");
    private static final String DB_PORT = getEnv("DB_PORT", "1433");
    private static final String DB_NAME = getEnv("DB_NAME", "StadiumDB");
    private static final String DB_USER = getEnv("DB_USER", null);
    private static final String DB_PASSWORD = getEnv("DB_PASSWORD", null);
    private static final String DB_ENCRYPT = getEnv("DB_ENCRYPT", "false");
    
    // Build connection URL from environment variables
    private static final String DB_URL = buildConnectionUrl();
    
    /**
     * Build connection URL based on whether using named instance or default instance
     * Named instances (e.g., localhost\\SQLEXPRESS) don't use port numbers
     * Default instances use host:port format
     */
    private static String buildConnectionUrl() {
        // If host contains instance name (backslash), don't add port
        if (DB_HOST.contains("\\")) {
            return String.format(
                "jdbc:sqlserver://%s;databaseName=%s;encrypt=%s;trustServerCertificate=true",
                DB_HOST, DB_NAME, DB_ENCRYPT
            );
        } else if (DB_PORT != null && !DB_PORT.trim().isEmpty()) {
            // Default instance with port
            return String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=true",
                DB_HOST, DB_PORT, DB_NAME, DB_ENCRYPT
            );
        } else {
            // Default instance without port
            return String.format(
                "jdbc:sqlserver://%s;databaseName=%s;encrypt=%s;trustServerCertificate=true",
                DB_HOST, DB_NAME, DB_ENCRYPT
            );
        }
    }
    
    /**
     * Get a NEW database connection each time
     * This prevents issues with closed connections
     * @return Connection object
     */
    public static Connection getConnection() {
        try {
            // Validate that required environment variables are set
            if (DB_USER == null || DB_USER.trim().isEmpty()) {
                throw new SQLException(
                    "❌ DB_USER not configured! Please create a .env file with database credentials.\n" +
                    "   Copy .env.example to .env and fill in your SQL Server username."
                );
            }
            
            if (DB_PASSWORD == null || DB_PASSWORD.trim().isEmpty()) {
                throw new SQLException(
                    "❌ DB_PASSWORD not configured! Please create a .env file with database credentials.\n" +
                    "   Copy .env.example to .env and fill in your SQL Server password."
                );
            }
            
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Database connection established successfully!");
            return connection;
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQL Server JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Failed to connect to the database!");
            System.err.println("   URL: " + DB_URL);
            System.err.println("   User: " + DB_USER);
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Close database connection
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection!");
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection
     * @return true if connection is successful, false otherwise
     */
    public static boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        } finally {
            closeConnection(conn);
        }
    }
}
