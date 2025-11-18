package org.openjfx.model;

/**
 * Model class representing Staff members
 * Used for staff management functionality
 */
public class Staff {
    private int staffId;
    private String fullName;
    private String position;
    private double salary;
    private String phoneNumber;
    private String address;
    private java.time.LocalDate hireDate;
    private boolean isActive;

    public Staff() {
        this.isActive = true;
        this.hireDate = java.time.LocalDate.now();
    }

    public Staff(int staffId, String fullName, String position, double salary, 
                 String phoneNumber, String address, java.time.LocalDate hireDate, boolean isActive) {
        this.staffId = staffId;
        this.fullName = fullName;
        this.position = position;
        this.salary = salary;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.hireDate = hireDate;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public java.time.LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(java.time.LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Staff{" +
                "staffId=" + staffId +
                ", fullName='" + fullName + '\'' +
                ", position='" + position + '\'' +
                ", salary=" + salary +
                ", isActive=" + isActive +
                '}';
    }
}
