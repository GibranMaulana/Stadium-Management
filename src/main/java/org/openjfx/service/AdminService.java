package org.openjfx.service;

import org.openjfx.model.Admin;
import org.openjfx.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Admin-related database operations
 * Enhanced with role-based access control
 */
public class AdminService {
    
    /**
     * Authenticate admin user
     * @param username Admin username
     * @param password Admin password
     * @return Admin object if authentication successful, null otherwise
     */
    public Admin authenticate(String username, String password) {
        String query = "SELECT AdminID, Username, Password, Role FROM Admins WHERE Username = ? AND Password = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("AdminID"));
                admin.setUsername(rs.getString("Username"));
                admin.setPassword(rs.getString("Password"));
                admin.setRole(rs.getString("Role")); // NEW: Get role from database
                return admin;
            }
            
        } catch (SQLException e) {
            System.err.println("Error during authentication!");
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Check if admin table exists and has data
     * @return true if table exists and has data, false otherwise
     */
    public boolean isAdminTableReady() {
        String query = "SELECT COUNT(*) as count FROM Admins";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Admin table might not exist yet.");
            return false;
        }
        
        return false;
    }
    
    /**
     * Get all admins (SUPER_ADMIN only)
     * @return List of all admins
     */
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String query = "SELECT AdminID, Username, Password, Role FROM Admins ORDER BY AdminID";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("AdminID"));
                admin.setUsername(rs.getString("Username"));
                admin.setPassword(rs.getString("Password"));
                admin.setRole(rs.getString("Role"));
                admins.add(admin);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all admins!");
            e.printStackTrace();
        }
        
        return admins;
    }
    
    /**
     * Create new admin (SUPER_ADMIN only)
     * @param username New admin username
     * @param password New admin password
     * @param role Admin role (ADMIN or SUPER_ADMIN)
     * @return true if successful, false otherwise
     */
    public boolean createAdmin(String username, String password, String role) {
        String query = "INSERT INTO Admins (Username, Password, Role) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error creating admin!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update admin role (SUPER_ADMIN only)
     * @param adminId Admin ID to update
     * @param newRole New role (ADMIN or SUPER_ADMIN)
     * @return true if successful, false otherwise
     */
    public boolean updateAdminRole(int adminId, String newRole) {
        String query = "UPDATE Admins SET Role = ? WHERE AdminID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, newRole);
            stmt.setInt(2, adminId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating admin role!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete admin (SUPER_ADMIN only)
     * @param adminId Admin ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteAdmin(int adminId) {
        String query = "DELETE FROM Admins WHERE AdminID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, adminId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting admin!");
            e.printStackTrace();
            return false;
        }
    }
}
