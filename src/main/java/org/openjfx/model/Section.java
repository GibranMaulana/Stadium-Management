package org.openjfx.model;

/**
 * Model class representing a stadium section (tribune or field zone)
 */
public class Section {
    private int sectionId;
    private String sectionName;
    private String sectionType; // TRIBUNE or FIELD
    private int totalRows;
    private int seatsPerRow;
    private int totalCapacity;
    
    // Constructors
    public Section() {}
    
    public Section(int sectionId, String sectionName, String sectionType, 
                   int totalRows, int seatsPerRow, int totalCapacity) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
        this.sectionType = sectionType;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        this.totalCapacity = totalCapacity;
    }
    
    // Getters and Setters
    public int getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
    
    public String getSectionName() {
        return sectionName;
    }
    
    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }
    
    public String getSectionType() {
        return sectionType;
    }
    
    public void setSectionType(String sectionType) {
        this.sectionType = sectionType;
    }
    
    public int getTotalRows() {
        return totalRows;
    }
    
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }
    
    public int getSeatsPerRow() {
        return seatsPerRow;
    }
    
    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }
    
    public int getTotalCapacity() {
        return totalCapacity;
    }
    
    public void setTotalCapacity(int totalCapacity) {
        this.totalCapacity = totalCapacity;
    }
    
    // Helper methods
    public boolean isTribune() {
        return "TRIBUNE".equals(sectionType);
    }
    
    public boolean isField() {
        return "FIELD".equals(sectionType);
    }
    
    @Override
    public String toString() {
        return sectionName;
    }
}
