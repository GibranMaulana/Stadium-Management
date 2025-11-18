package org.openjfx.model;

/**
 * Admin model class for authentication
 * Enhanced with role-based access control
 */
public class Admin {
    private int id;
    private String username;
    private String password;
    private String role; // NEW: ADMIN or SUPER_ADMIN
    
    public Admin() {
        this.role = "ADMIN"; // Default role
    }
    
    public Admin(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = "ADMIN"; // Default role
    }
    
    public Admin(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    // NEW: Role getter/setter
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // Helper methods for role checking
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }
    
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
    
    @Override
    public String toString() {
        return "Admin{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
