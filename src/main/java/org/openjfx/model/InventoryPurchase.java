package org.openjfx.model;

import java.time.LocalDateTime;

public class InventoryPurchase {
    private int purchaseId;
    private Integer itemId;
    private Integer eventId;
    private int quantity;
    private double unitCost;
    private double totalCost;
    private LocalDateTime purchaseDate;
    private String supplier;
    private String notes;

    public InventoryPurchase() {}

    public InventoryPurchase(Integer itemId, Integer eventId, int quantity, double unitCost, String supplier, String notes) {
        this.itemId = itemId;
        this.eventId = eventId;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalCost = quantity * unitCost;
        this.purchaseDate = LocalDateTime.now();
        this.supplier = supplier;
        this.notes = notes;
    }

    public int getPurchaseId() { return purchaseId; }
    public void setPurchaseId(int purchaseId) { this.purchaseId = purchaseId; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; this.totalCost = this.unitCost * quantity; }

    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; this.totalCost = this.unitCost * this.quantity; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "InventoryPurchase{" +
                "purchaseId=" + purchaseId +
                ", itemId=" + itemId +
                ", eventId=" + eventId +
                ", quantity=" + quantity +
                ", unitCost=" + unitCost +
                ", totalCost=" + totalCost +
                ", purchaseDate=" + purchaseDate +
                ", supplier='" + supplier + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
