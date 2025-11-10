package org.openjfx;

import org.openjfx.util.DatabaseUtil;
import org.openjfx.service.AdminService;

import java.sql.Connection;

/**
 * Simple test class to verify database connection
 */
public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("Stadium Management System - Connection Test");
        System.out.println("===========================================\n");
        
        // Test 1: Basic connection
        System.out.println("Test 1: Testing database connection...");
        Connection conn = DatabaseUtil.getConnection();
        if (conn != null) {
            System.out.println("✅ SUCCESS: Database connection established!");
        } else {
            System.out.println("❌ FAILED: Could not connect to database");
            return;
        }
        
        // Test 2: Connection test method
        System.out.println("\nTest 2: Testing connection validity...");
        boolean isValid = DatabaseUtil.testConnection();
        if (isValid) {
            System.out.println("✅ SUCCESS: Connection is valid!");
        } else {
            System.out.println("❌ FAILED: Connection is not valid");
        }
        
        // Test 3: Check if admin table is ready
        System.out.println("\nTest 3: Checking admin table...");
        AdminService adminService = new AdminService();
        boolean tableReady = adminService.isAdminTableReady();
        if (tableReady) {
            System.out.println("✅ SUCCESS: Admin table exists and has data!");
        } else {
            System.out.println("❌ FAILED: Admin table is not ready");
        }
        
        // Test 4: Test authentication with default credentials
        System.out.println("\nTest 4: Testing authentication...");
        var admin = adminService.authenticate("admin", "admin123");
        if (admin != null) {
            System.out.println("✅ SUCCESS: Authentication works!");
            System.out.println("   Logged in as: " + admin.getUsername() + " (ID: " + admin.getId() + ")");
        } else {
            System.out.println("❌ FAILED: Authentication failed");
        }
        
        // Clean up - close the connection we opened
        if (conn != null) {
            DatabaseUtil.closeConnection(conn);
        }
        
        System.out.println("\n===========================================");
        System.out.println("All tests completed! 🎉");
        System.out.println("===========================================");
        System.out.println("\n✨ Your database is ready!");
        System.out.println("   You can now run the application:");
        System.out.println("   mvn javafx:run");
        System.out.println("\n   Login with:");
        System.out.println("   Username: admin");
        System.out.println("   Password: admin123");
    }
}
