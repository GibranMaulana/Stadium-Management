package org.openjfx.util;

import org.openjfx.model.Admin;
import org.openjfx.service.AdminService;

/**
 * Quick test for AdminService with PascalCase columns
 */
public class TestAdminService {
    
    public static void main(String[] args) {
        System.out.println("=== Testing AdminService ===\n");
        
        AdminService adminService = new AdminService();
        
        // Test authentication
        System.out.println("Test: Authenticate with admin/admin123");
        Admin admin = adminService.authenticate("admin", "admin123");
        
        if (admin != null) {
            System.out.println("✅ SUCCESS: Authentication works!");
            System.out.println("   AdminID: " + admin.getId());
            System.out.println("   Username: " + admin.getUsername());
        } else {
            System.out.println("❌ FAILED: Authentication failed");
        }
        
        // Test table ready
        System.out.println("\nTest: Check if admin table is ready");
        boolean isReady = adminService.isAdminTableReady();
        
        if (isReady) {
            System.out.println("✅ SUCCESS: Admin table is ready with data");
        } else {
            System.out.println("❌ FAILED: Admin table is not ready");
        }
        
        System.out.println("\n=== All Tests Completed ===");
    }
}
