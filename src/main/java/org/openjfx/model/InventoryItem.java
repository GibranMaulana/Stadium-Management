package org.openjfx.model;

/**
 * Model class representing Inventory Items
 * Used for stadium inventory management
 */
public class InventoryItem {
    private int itemId;
    private String itemName;
    private String description;
    private String category;
    private int quantity;
    private int minStockLevel;
    private double unitPrice;
    private String location;

    public InventoryItem() {
        this.minStockLevel = 10;
        this.unitPrice = 0.0;
    }

    public InventoryItem(int itemId, String itemName, String description, 
                         int quantity, int minStockLevel, String location) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.location = location;
        this.category = "Other"; // default
        this.unitPrice = 0.0;
    }

    // Full constructor with all fields including Category and UnitPrice
    public InventoryItem(int itemId, String itemName, String description, 
                         int quantity, int minStockLevel, String location,
                         String category, double unitPrice) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.quantity = quantity;
        this.minStockLevel = minStockLevel;
        this.location = location;
        this.category = category;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinStockLevel() {
        return minStockLevel;
    }

    public void setMinStockLevel(int minStockLevel) {
        this.minStockLevel = minStockLevel;
    }

    // Alias methods for UI consistency
    public int getMinimumStock() {
        return minStockLevel;
    }

    public void setMinimumStock(int minimumStock) {
        this.minStockLevel = minimumStock;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // Helper methods
    public boolean isLowStock() {
        return quantity < minStockLevel;
    }

    public int getShortageAmount() {
        return Math.max(0, minStockLevel - quantity);
    }

    public String getStockStatus() {
        if (quantity == 0) return "OUT_OF_STOCK";
        if (isLowStock()) return "LOW_STOCK";
        return "IN_STOCK";
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "itemId=" + itemId +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", minStockLevel=" + minStockLevel +
                ", unitPrice=" + unitPrice +
                ", status=" + getStockStatus() +
                '}';
    }
}
