package org.openjfx.service;

import org.openjfx.model.Admin;
import org.openjfx.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service class for Admin-related database operations
 */
public class AdminService {
    
    /**
     * Authenticate admin user
     * @param username Admin username
     * @param password Admin password
     * @return Admin object if authentication successful, null otherwise
     */
    public Admin authenticate(String username, String password) {
        String query = "SELECT AdminID, Username, Password FROM Admins WHERE Username = ? AND Password = ?";
        
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
}
