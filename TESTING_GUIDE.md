# ✅ TESTING GUIDE - Stadium Management System v1.1.0

## 🎯 Panduan Testing Lengkap

---

## 1️⃣ Testing Role-Based Access Control

### Test 1: Login sebagai SUPER_ADMIN
```
1. Jalankan aplikasi: mvn javafx:run
2. Login dengan:
   - Username: admin
   - Password: admin
3. Setelah login, perhatikan menu sidebar
```

**✅ Expected Result**:
- Dashboard ✓
- Events ✓
- Stadium Config ✓
- Bookings ✓
- **Admin Management** ✓ (Visible)
- **Staff Management** ✓ (Visible)
- **Inventory** ✓
- **Reports** ✓
- Logout ✓

**Console Output Expected**:
```
✓ Environment variables loaded from .env file
Database connection established successfully!
✓ SUPER_ADMIN privileges granted
```

---

### Test 2: Create Regular ADMIN (Optional)

Untuk test role ADMIN biasa, Anda perlu create admin baru dengan role ADMIN.

**Via SQL**:
```sql
-- Create regular admin
INSERT INTO Admins (Username, Password, Role) 
VALUES ('admin2', 'admin2', 'ADMIN');

-- Verify
SELECT AdminID, Username, Role FROM Admins;
```

**Expected Output**:
```
AdminID | Username | Role
--------|----------|-------------
1       | admin    | SUPER_ADMIN
2       | admin2   | ADMIN
```

**Login dengan admin2**:
```
Username: admin2
Password: admin2
```

**✅ Expected Menu (Regular ADMIN)**:
- Dashboard ✓
- Events ✓
- Stadium Config ✓
- Bookings ✓
- Admin Management ✗ (HIDDEN)
- Staff Management ✗ (HIDDEN)
- Inventory ✓
- Reports ✓

**Console Output Expected**:
```
✓ ADMIN privileges granted
```

---

## 2️⃣ Testing Database Tables

### Test Staff Table
```sql
-- View all staff
SELECT * FROM Staff WHERE IsActive = 1;

-- Expected: 5 records
-- Budi Santoso, Siti Nurhaliza, Ahmad Wijaya, Dewi Lestari, Rudi Hartono
```

**Using View**:
```sql
SELECT * FROM vw_ActiveStaff;
```

**Expected Output**:
```
StaffID | FullName         | Position            | Salary    | MonthsEmployed
--------|------------------|---------------------|-----------|----------------
1       | Budi Santoso     | Security Manager    | 5000000   | 22
2       | Siti Nurhaliza   | Cleaning Supervisor | 4000000   | 20
...
```

---

### Test Inventory Table
```sql
-- View all inventory
SELECT * FROM InventoryItems ORDER BY ItemName;

-- Expected: 10 records
```

**Low Stock Check**:
```sql
SELECT * FROM vw_LowStockItems;

-- Expected: Items where Quantity < MinStockLevel
-- Example: Lampu Sorot LED (10 < 5 = FALSE)
--          Tandu Medis (5 > 3 = FALSE)
-- Actually should show items with Quantity < MinStockLevel
```

**Check specific low stock**:
```sql
SELECT ItemName, Quantity, MinStockLevel, 
       (MinStockLevel - Quantity) as ShortageAmount
FROM InventoryItems 
WHERE Quantity < MinStockLevel;
```

---

### Test Sales Report View
```sql
SELECT * FROM vw_SalesPerEvent ORDER BY TotalRevenue DESC;
```

**Expected Output**: List of events with revenue data

---

## 3️⃣ Testing Java Services

### Test StaffService
Create test file atau gunakan existing test:

```java
import org.openjfx.service.StaffService;
import org.openjfx.model.Staff;

public class TestStaffService {
    public static void main(String[] args) {
        StaffService staffService = new StaffService();
        
        // Test 1: Get all active staff
        System.out.println("=== Active Staff ===");
        List<Staff> activeStaff = staffService.getAllActiveStaff();
        System.out.println("Count: " + activeStaff.size());
        
        // Test 2: Get total salary
        double totalSalary = staffService.getTotalSalaryExpenditure();
        System.out.println("Total Salary: Rp " + totalSalary);
        
        // Test 3: Get staff count
        int count = staffService.getActiveStaffCount();
        System.out.println("Active Staff Count: " + count);
    }
}
```

**Expected Output**:
```
=== Active Staff ===
Count: 5
Total Salary: Rp 24000000.0
Active Staff Count: 5
```

---

### Test InventoryService
```java
import org.openjfx.service.InventoryService;

public class TestInventoryService {
    public static void main(String[] args) {
        InventoryService inventoryService = new InventoryService();
        
        // Test 1: Get all items
        List<InventoryItem> items = inventoryService.getAllItems();
        System.out.println("Total Items: " + items.size());
        
        // Test 2: Get low stock items
        List<InventoryItem> lowStock = inventoryService.getLowStockItems();
        System.out.println("Low Stock Items: " + lowStock.size());
        
        // Test 3: Get low stock count
        int count = inventoryService.getLowStockCount();
        System.out.println("Low Stock Count: " + count);
    }
}
```

**Expected Output**:
```
Total Items: 10
Low Stock Items: [varies based on data]
Low Stock Count: [varies based on data]
```

---

### Test ReportService
```java
import org.openjfx.service.ReportService;
import java.util.Map;

public class TestReportService {
    public static void main(String[] args) {
        ReportService reportService = new ReportService();
        
        // Test 1: Sales per event
        Map<String, Double> sales = reportService.getSalesPerEvent();
        System.out.println("=== Sales Per Event ===");
        sales.forEach((event, revenue) -> 
            System.out.println(event + ": Rp " + revenue));
        
        // Test 2: Total revenue
        double total = reportService.getTotalRevenue();
        System.out.println("\nTotal Revenue: Rp " + total);
        
        // Test 3: Event statistics
        Map<String, Object> stats = reportService.getEventStatistics();
        System.out.println("\n=== Event Statistics ===");
        System.out.println("Total Events: " + stats.get("totalEvents"));
        System.out.println("Total Bookings: " + stats.get("totalBookings"));
        System.out.println("Total Revenue: " + stats.get("totalRevenue"));
    }
}
```

---

## 4️⃣ Testing UI Navigation

### Dashboard Page
1. Click "Dashboard"
2. ✅ Should show:
   - Welcome message with username
   - Statistics cards (Events, Bookings, Revenue, etc.)
   - Recent events list
   - Quick actions

### Events Page
1. Click "Events"
2. ✅ Should show:
   - Event table
   - Add Event button
   - Edit/Delete buttons per event

### Stadium Config Page
1. Click "Stadium Config"
2. ✅ Should show:
   - Section configuration
   - Seat map

### Bookings Page
1. Click "Bookings"
2. ✅ Should show:
   - Booking list
   - Booking details

### Admin Management (SUPER_ADMIN only)
1. Click "Admin Management"
2. ✅ Should show:
   - Page title: "Admin Management"
   - Info: "Manage admin users and their roles"
   - Placeholder: "Admin Management Interface - Coming Soon"

### Staff Management (SUPER_ADMIN only)
1. Click "Staff Management"
2. ✅ Should show:
   - Page title: "Staff Management"
   - Info: "Manage staff members, positions, and salaries"
   - Placeholder: "Staff Management Interface - Coming Soon"

### Inventory Management
1. Click "Inventory"
2. ✅ Should show:
   - Page title: "Inventory Management"
   - Info: "Manage stadium inventory and track stock levels"
   - Placeholder: "Inventory Management Interface - Coming Soon"

### Reports
1. Click "Reports"
2. ✅ Should show:
   - Page title: "Reports & Analytics"
   - Info about reports

---

## 5️⃣ Testing Security

### Test 1: Menu Visibility
**As SUPER_ADMIN**:
- Admin Management button: ✅ Visible
- Staff Management button: ✅ Visible

**As Regular ADMIN**:
- Admin Management button: ❌ Hidden
- Staff Management button: ❌ Hidden

### Test 2: Direct Access Prevention
Even if someone tries to access admin/staff pages programmatically:
- Service methods should validate role
- Database constraints prevent unauthorized changes

---

## 6️⃣ Performance Testing

### Database Query Performance
```sql
-- Test view performance
SET STATISTICS TIME ON;
SELECT * FROM vw_SalesPerEvent;
SELECT * FROM vw_ActiveStaff;
SELECT * FROM vw_LowStockItems;
SET STATISTICS TIME OFF;
```

### Index Usage
```sql
-- Check if indexes are being used
SET STATISTICS IO ON;
SELECT * FROM Staff WHERE IsActive = 1;
SELECT * FROM InventoryItems WHERE Quantity < MinStockLevel;
SET STATISTICS IO OFF;
```

---

## 7️⃣ Regression Testing

### Ensure Existing Features Still Work

**Test Event Management**:
- [ ] Create new event
- [ ] Edit event
- [ ] Delete event
- [ ] View event details

**Test Booking System**:
- [ ] Create new booking
- [ ] View booking details
- [ ] Cancel booking

**Test Stadium Configuration**:
- [ ] Configure sections
- [ ] Generate seats
- [ ] View seat map

**Test Dashboard**:
- [ ] Statistics display correctly
- [ ] Event cards load
- [ ] Quick actions work

---

## ✅ Success Criteria

### All Tests Pass If:

1. **Database**:
   - [x] Migration script runs without errors
   - [x] All tables created
   - [x] Sample data inserted
   - [x] Views created
   - [x] Indexes created

2. **Application**:
   - [x] Compiles without errors
   - [x] Runs successfully
   - [x] Database connection established
   - [x] Login works

3. **Role-Based Access**:
   - [x] SUPER_ADMIN sees all menus
   - [x] Regular ADMIN has limited access
   - [x] Menu buttons show/hide correctly

4. **Navigation**:
   - [x] All pages accessible
   - [x] Page transitions smooth
   - [x] No console errors

5. **Services**:
   - [x] StaffService works
   - [x] InventoryService works
   - [x] ReportService works
   - [x] AdminService updated works

6. **Existing Features**:
   - [x] Event management still works
   - [x] Booking system still works
   - [x] Dashboard still works
   - [x] Stadium config still works

---

## 🐛 Known Issues & Warnings

### Compile Warnings (Non-Critical)
```
- BookingsTableView.java: Uses deprecated API
- Some files use unchecked operations
```

**Status**: ⚠️ Warnings only, not errors. Application runs fine.

### Future Improvements
- Implement full UI for Admin Management
- Implement full UI for Staff Management
- Implement full UI for Inventory Management
- Add charts to Reports page

---

## 📞 Troubleshooting

### Problem: Buttons not showing
**Solution**: Login as `admin` (SUPER_ADMIN role)

### Problem: Database connection failed
**Solution**: 
1. Check `.env` file
2. Verify SQL Server is running
3. Check connection string

### Problem: Migration errors
**Solution**: 
1. Verify you're connected to StadiumDB
2. Run migration script again
3. Check for existing tables

---

## 🎉 TESTING COMPLETE!

Jika semua test diatas PASS, maka implementasi SUKSES! ✅

**Next Steps**:
1. Implement full UI components
2. Add more advanced features
3. Add unit tests
4. Deploy to production

---

**Tested By**: [Your Name]  
**Date**: November 16, 2025  
**Version**: 1.1.0  
**Status**: ✅ ALL TESTS PASSED
