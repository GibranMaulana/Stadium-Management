# 🚀 Quick Start Guide - Stadium Management System v1.1.0

## ⚡ Setup dalam 3 Langkah

### 1️⃣ Setup Database
```bash
# Jalankan migration script
sqlcmd -S localhost -U sa -P tigasatuagus -i database\03_features_roles_staff_inventory.sql
```

**Hasil yang Diharapkan**:
```
✓ Role column added to Admins table
✓ Default admin upgraded to SUPER_ADMIN
✓ Staff table created
✓ InventoryItems table created
✓ Sample staff data inserted (5 records)
✓ Sample inventory data inserted (10 records)
✓ Indexes created
✓ Views created
```

---

### 2️⃣ Jalankan Aplikasi
```bash
# Compile dan run
mvn clean javafx:run
```

**Atau** jika sudah di-compile:
```bash
mvn javafx:run
```

---

### 3️⃣ Login & Test

**Login sebagai SUPER_ADMIN**:
- Username: `admin`
- Password: `admin`
- **Yang Terlihat**: ✅ Admin Management, ✅ Staff Management, ✅ All Features

**Test Navigation**:
1. Klik "Dashboard" → Lihat statistik
2. Klik "Events" → Manage events
3. Klik "Admin Management" → (SUPER_ADMIN only)
4. Klik "Staff Management" → (SUPER_ADMIN only)
5. Klik "Inventory" → (Available to all)
6. Klik "Reports" → (Available to all)

---

## 🎯 Fitur Baru yang Tersedia

### ✨ Role-Based Access
- **SUPER_ADMIN**: Full access
- **ADMIN**: Limited access (no admin/staff management)

### 👥 Staff Management
- View: 5 sample staff members
- Positions: Security Manager, Cleaning Supervisor, Ticketing Staff, dll.
- Salaries tracked

### 📦 Inventory Management
- View: 10 inventory items
- Low stock alerts
- Locations tracked

### 📊 Reports
- Sales per event
- Revenue analytics
- Inventory reports

---

## 🔍 Verifikasi Database

### Check Role Column
```sql
SELECT AdminID, Username, Role FROM Admins;
```

**Expected**:
```
AdminID | Username | Role
--------|----------|-------------
1       | admin    | SUPER_ADMIN
```

### Check Staff Data
```sql
SELECT COUNT(*) as StaffCount FROM Staff WHERE IsActive = 1;
```

**Expected**: `5`

### Check Inventory Data
```sql
SELECT COUNT(*) as ItemCount FROM InventoryItems;
```

**Expected**: `10`

### Check Low Stock Items
```sql
SELECT * FROM vw_LowStockItems;
```

---

## 🐛 Troubleshooting

### Aplikasi tidak bisa login?
1. Check database connection di `.env`
2. Pastikan SQL Server running
3. Verify migration script sudah dijalankan

### Tombol Admin/Staff tidak muncul?
- Login sebagai `admin` (SUPER_ADMIN)
- Regular admin tidak akan melihat tombol ini

### Error saat compile?
```bash
mvn clean install
mvn javafx:run
```

---

## 📚 Dokumentasi Lengkap

Lihat `IMPLEMENTATION_SUMMARY.md` untuk detail lengkap.

---

**Happy Coding! 🎉**
