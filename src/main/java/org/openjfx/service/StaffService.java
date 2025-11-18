package org.openjfx.service;

import org.openjfx.model.Staff;
import org.openjfx.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Staff management operations
 * Handles CRUD operations for staff members
 */
public class StaffService {
    
    /**
     * Get all active staff members
     * @return List of active staff
     */
    public List<Staff> getAllActiveStaff() {
        List<Staff> staffList = new ArrayList<>();
        String query = "SELECT StaffID, FullName, Position, Salary, PhoneNumber, Address, HireDate, IsActive " +
                      "FROM Staff WHERE IsActive = 1 ORDER BY FullName";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setStaffId(rs.getInt("StaffID"));
                staff.setFullName(rs.getString("FullName"));
                staff.setPosition(rs.getString("Position"));
                staff.setSalary(rs.getDouble("Salary"));
                staff.setPhoneNumber(rs.getString("PhoneNumber"));
                staff.setAddress(rs.getString("Address"));
                
                Date hireDate = rs.getDate("HireDate");
                if (hireDate != null) {
                    staff.setHireDate(hireDate.toLocalDate());
                }
                
                staff.setActive(rs.getBoolean("IsActive"));
                staffList.add(staff);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting active staff!");
            e.printStackTrace();
        }
        
        return staffList;
    }
    
    /**
     * Get all staff members (including inactive)
     * @return List of all staff
     */
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String query = "SELECT StaffID, FullName, Position, Salary, PhoneNumber, Address, HireDate, IsActive " +
                      "FROM Staff ORDER BY IsActive DESC, FullName";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setStaffId(rs.getInt("StaffID"));
                staff.setFullName(rs.getString("FullName"));
                staff.setPosition(rs.getString("Position"));
                staff.setSalary(rs.getDouble("Salary"));
                staff.setPhoneNumber(rs.getString("PhoneNumber"));
                staff.setAddress(rs.getString("Address"));
                
                Date hireDate = rs.getDate("HireDate");
                if (hireDate != null) {
                    staff.setHireDate(hireDate.toLocalDate());
                }
                
                staff.setActive(rs.getBoolean("IsActive"));
                staffList.add(staff);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all staff!");
            e.printStackTrace();
        }
        
        return staffList;
    }
    
    /**
     * Add new staff member
     * @param staff Staff object to add
     * @return true if successful, false otherwise
     */
    public boolean addStaff(Staff staff) {
        String query = "INSERT INTO Staff (FullName, Position, Salary, PhoneNumber, Address, HireDate, IsActive) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, staff.getFullName());
            stmt.setString(2, staff.getPosition());
            stmt.setDouble(3, staff.getSalary());
            stmt.setString(4, staff.getPhoneNumber());
            stmt.setString(5, staff.getAddress());
            stmt.setDate(6, Date.valueOf(staff.getHireDate()));
            stmt.setBoolean(7, staff.isActive());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding staff!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update existing staff member
     * @param staff Staff object with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateStaff(Staff staff) {
        String query = "UPDATE Staff SET FullName = ?, Position = ?, Salary = ?, " +
                      "PhoneNumber = ?, Address = ?, HireDate = ?, IsActive = ?, UpdatedAt = GETDATE() " +
                      "WHERE StaffID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, staff.getFullName());
            stmt.setString(2, staff.getPosition());
            stmt.setDouble(3, staff.getSalary());
            stmt.setString(4, staff.getPhoneNumber());
            stmt.setString(5, staff.getAddress());
            stmt.setDate(6, Date.valueOf(staff.getHireDate()));
            stmt.setBoolean(7, staff.isActive());
            stmt.setInt(8, staff.getStaffId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating staff!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Deactivate staff member (soft delete)
     * @param staffId Staff ID to deactivate
     * @return true if successful, false otherwise
     */
    public boolean deactivateStaff(int staffId) {
        String query = "UPDATE Staff SET IsActive = 0, UpdatedAt = GETDATE() WHERE StaffID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, staffId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deactivating staff!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Activate staff member
     * @param staffId Staff ID to activate
     * @return true if successful, false otherwise
     */
    public boolean activateStaff(int staffId) {
        String query = "UPDATE Staff SET IsActive = 1, UpdatedAt = GETDATE() WHERE StaffID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, staffId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error activating staff!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete staff member permanently (use with caution)
     * @param staffId Staff ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteStaff(int staffId) {
        String query = "DELETE FROM Staff WHERE StaffID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, staffId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting staff!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get total salary expenditure for active staff
     * @return Total salary amount
     */
    public double getTotalSalaryExpenditure() {
        String query = "SELECT SUM(Salary) as TotalSalary FROM Staff WHERE IsActive = 1";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("TotalSalary");
            }
            
        } catch (SQLException e) {
            System.err.println("Error calculating total salary!");
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Get staff count by position
     * @return Total number of active staff
     */
    public int getActiveStaffCount() {
        String query = "SELECT COUNT(*) as Count FROM Staff WHERE IsActive = 1";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting staff!");
            e.printStackTrace();
        }
        
        return 0;
    }
}
