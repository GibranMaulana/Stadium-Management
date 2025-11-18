package org.openjfx.service;

import org.openjfx.model.InventoryItem;
import org.openjfx.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Inventory management operations
 * Handles CRUD operations for inventory items
 */
public class InventoryService {
    
    /**
     * Get all inventory items
     * @return List of all inventory items
     */
    public List<InventoryItem> getAllItems() {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT ItemID, ItemName, Description, Category, Quantity, MinStockLevel, UnitPrice, Location " +
                      "FROM InventoryItems ORDER BY ItemName";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setItemId(rs.getInt("ItemID"));
                item.setItemName(rs.getString("ItemName"));
                item.setDescription(rs.getString("Description"));
                item.setCategory(rs.getString("Category"));
                item.setQuantity(rs.getInt("Quantity"));
                item.setMinStockLevel(rs.getInt("MinStockLevel"));
                item.setUnitPrice(rs.getDouble("UnitPrice"));
                item.setLocation(rs.getString("Location"));
                items.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting inventory items!");
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Get low stock items (quantity below minimum)
     * @return List of items with low stock
     */
    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT ItemID, ItemName, Description, Quantity, MinStockLevel, Location " +
                      "FROM InventoryItems WHERE Quantity < MinStockLevel ORDER BY Quantity";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setItemId(rs.getInt("ItemID"));
                item.setItemName(rs.getString("ItemName"));
                item.setDescription(rs.getString("Description"));
                item.setQuantity(rs.getInt("Quantity"));
                item.setMinStockLevel(rs.getInt("MinStockLevel"));
                item.setLocation(rs.getString("Location"));
                items.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting low stock items!");
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Get items by location
     * @param location Location to filter by
     * @return List of items in the specified location
     */
    public List<InventoryItem> getItemsByLocation(String location) {
        List<InventoryItem> items = new ArrayList<>();
        String query = "SELECT ItemID, ItemName, Description, Quantity, MinStockLevel, Location " +
                      "FROM InventoryItems WHERE Location = ? ORDER BY ItemName";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, location);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                InventoryItem item = new InventoryItem();
                item.setItemId(rs.getInt("ItemID"));
                item.setItemName(rs.getString("ItemName"));
                item.setDescription(rs.getString("Description"));
                item.setQuantity(rs.getInt("Quantity"));
                item.setMinStockLevel(rs.getInt("MinStockLevel"));
                item.setLocation(rs.getString("Location"));
                items.add(item);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting items by location!");
            e.printStackTrace();
        }
        
        return items;
    }
    
    /**
     * Add new inventory item
     * @param item Inventory item to add
     * @return true if successful, false otherwise
     */
    public boolean addItem(InventoryItem item) {
        String query = "INSERT INTO InventoryItems (ItemName, Description, Category, Quantity, MinStockLevel, UnitPrice, Location) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getCategory());
            stmt.setInt(4, item.getQuantity());
            stmt.setInt(5, item.getMinStockLevel());
            stmt.setDouble(6, item.getUnitPrice());
            stmt.setString(7, item.getLocation());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding inventory item!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update existing inventory item
     * @param item Inventory item with updated information
     * @return true if successful, false otherwise
     */
    public boolean updateItem(InventoryItem item) {
        String query = "UPDATE InventoryItems SET ItemName = ?, Description = ?, Category = ?, Quantity = ?, " +
                      "MinStockLevel = ?, UnitPrice = ?, Location = ?, UpdatedAt = GETDATE() WHERE ItemID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, item.getItemName());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getCategory());
            stmt.setInt(4, item.getQuantity());
            stmt.setInt(5, item.getMinStockLevel());
            stmt.setDouble(6, item.getUnitPrice());
            stmt.setString(7, item.getLocation());
            stmt.setInt(8, item.getItemId());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating inventory item!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update only the quantity of an item
     * @param itemId Item ID to update
     * @param newQuantity New quantity value
     * @return true if successful, false otherwise
     */
    public boolean updateItemQuantity(int itemId, int newQuantity) {
        String query = "UPDATE InventoryItems SET Quantity = ?, UpdatedAt = GETDATE() WHERE ItemID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, itemId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating item quantity!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Increase item quantity (for restocking)
     * @param itemId Item ID
     * @param amount Amount to add
     * @return true if successful, false otherwise
     */
    public boolean increaseQuantity(int itemId, int amount) {
        String query = "UPDATE InventoryItems SET Quantity = Quantity + ?, UpdatedAt = GETDATE() WHERE ItemID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, amount);
            stmt.setInt(2, itemId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error increasing item quantity!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Decrease item quantity (for usage)
     * @param itemId Item ID
     * @param amount Amount to subtract
     * @return true if successful, false otherwise
     */
    public boolean decreaseQuantity(int itemId, int amount) {
        String query = "UPDATE InventoryItems SET Quantity = Quantity - ?, UpdatedAt = GETDATE() " +
                      "WHERE ItemID = ? AND Quantity >= ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, amount);
            stmt.setInt(2, itemId);
            stmt.setInt(3, amount); // Prevent negative quantities
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error decreasing item quantity!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete inventory item
     * @param itemId Item ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteItem(int itemId) {
        String query = "DELETE FROM InventoryItems WHERE ItemID = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, itemId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting inventory item!");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get count of low stock items
     * @return Number of items below minimum stock level
     */
    public int getLowStockCount() {
        String query = "SELECT COUNT(*) as Count FROM InventoryItems WHERE Quantity < MinStockLevel";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting low stock items!");
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Get total number of inventory items
     * @return Total item count
     */
    public int getTotalItemCount() {
        String query = "SELECT COUNT(*) as Count FROM InventoryItems";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting total items!");
            e.printStackTrace();
        }
        
        return 0;
    }
}
