# Dokumentasi Sistem Manajemen Stadion

## Daftar Isi
1. [Pendahuluan](#pendahuluan)
2. [Arsitektur Sistem](#arsitektur-sistem)
3. [Struktur Database](#struktur-database)
4. [Struktur Kode Sumber](#struktur-kode-sumber)
5. [Modul Utama](#modul-utama)

---

## Pendahuluan

### Deskripsi Sistem
Sistem Manajemen Stadion merupakan aplikasi berbasis desktop yang dikembangkan menggunakan Java dengan framework JavaFX. Sistem ini dirancang untuk memfasilitasi pengelolaan operasional stadion secara menyeluruh, mencakup pemesanan tiket offline, manajemen inventaris, administrasi pengguna, dan analisis laporan.

### Teknologi yang Digunakan
- **Bahasa Pemrograman**: Java 11+
- **Framework UI**: JavaFX 25
- **Build Tool**: Apache Maven
- **Database**: Microsoft SQL Server
- **Library Tambahan**:
  - FontAwesome (Ikon UI)
  - JavaMail (Pengiriman Email)
  - Dotenv (Konfigurasi Environment)
  - MSSQL JDBC Driver (Koneksi Database)

### Pengguna Sistem
Sistem ini diperuntukkan bagi:
1. **Administrator**: Pengelola sistem dengan akses penuh
2. **Staff Stadion**: Operator pemesanan tiket dan manajemen inventaris
3. **Manager**: Pengawas dengan akses laporan dan analisis

---

## Arsitektur Sistem

### Pola Arsitektur
Sistem mengimplementasikan pola **Model-View-Controller (MVC)** dengan struktur modular:

```
┌─────────────┐
│    View     │ ← JavaFX Components
│ (Component) │
└──────┬──────┘
       │
┌──────▼──────┐
│ Controller  │ ← Event Handlers
└──────┬──────┘
       │
┌──────▼──────┐
│   Service   │ ← Business Logic
└──────┬──────┘
       │
┌──────▼──────┐
│    Model    │ ← Data Objects
└──────┬──────┘
       │
┌──────▼──────┐
│  Database   │ ← SQL Server
└─────────────┘
```

---

## Struktur Database

### Direktori: `database/`

Direktori ini berisi skrip SQL untuk inisialisasi dan migrasi database.

#### File: `01_initial_setup.sql`
**Fungsi**: Skrip inisialisasi database utama yang membuat tabel-tabel fundamental sistem.

**Tabel yang Dibuat**:
1. **Admins**: Menyimpan data administrator dan staff
2. **Events**: Menyimpan informasi event/pertandingan
3. **Sections**: Menyimpan informasi seksi stadion
4. **Seats**: Menyimpan data kursi per seksi
5. **EventSections**: Konfigurasi harga per event dan seksi
6. **Bookings**: Data pemesanan tiket
7. **BookingSeats**: Detail kursi yang dipesan

**Contoh Struktur Tabel**:
```sql
CREATE TABLE Events (
    EventID INT IDENTITY(1,1) PRIMARY KEY,
    EventName NVARCHAR(200) NOT NULL,
    EventType VARCHAR(50) NOT NULL,
    EventDate DATE NOT NULL,
    EventTime TIME NOT NULL,
    Description NVARCHAR(MAX),
    Status VARCHAR(20) DEFAULT 'UPCOMING',
    TotalSeats INT NOT NULL,
    BookedSeats INT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE()
);
```

#### File: `02_sync_seats.sql`
**Fungsi**: Skrip untuk sinkronisasi data kursi dengan seksi stadion.

**Cara Kerja**:
- Memastikan setiap kursi memiliki referensi yang valid ke seksi
- Menghapus data kursi yang tidak valid
- Memperbaiki inkonsistensi data

#### File: `03_features_roles_staff_inventory.sql`
**Fungsi**: Menambahkan fitur manajemen staff dan inventaris.

**Tabel yang Ditambahkan**:
1. **Staff**: Data karyawan stadion
2. **InventoryItems**: Item inventaris stadion
3. **AdminRoles**: Perluasan role administrator

**Struktur Tabel Staff**:
```sql
CREATE TABLE Staff (
    StaffID INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(200) NOT NULL,
    Position NVARCHAR(100) NOT NULL,
    Salary DECIMAL(18,2) NOT NULL,
    Phone NVARCHAR(20),
    Email NVARCHAR(200),
    HireDate DATE NOT NULL,
    Status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

#### File: `04_add_inventory_fields.sql`
**Fungsi**: Menambahkan field kategori dan harga satuan pada tabel inventaris.

**Field yang Ditambahkan**:
```sql
ALTER TABLE InventoryItems
ADD Category NVARCHAR(100) NULL,
    UnitPrice DECIMAL(18,2) NULL;
```

#### File: `06_event_expenses.sql`
**Fungsi**: Membuat tabel untuk mencatat pengeluaran per event.

**Struktur Tabel**:
```sql
CREATE TABLE EventExpenses (
    ExpenseID INT IDENTITY(1,1) PRIMARY KEY,
    EventID INT NOT NULL,
    ExpenseType VARCHAR(50) NOT NULL,
    ItemID INT NULL,
    Quantity INT NULL,
    UnitCost DECIMAL(18,2) NULL,
    TotalCost DECIMAL(18,2) NOT NULL,
    Notes NVARCHAR(1000) NULL,
    CreatedAt DATETIME DEFAULT GETDATE()
);
```

**Kegunaan**: Memungkinkan pencatatan biaya operasional per event seperti kerusakan inventaris, biaya tambahan, dll.

---

## Struktur Kode Sumber

### Direktori: `src/main/java/org/openjfx/`

#### Subdirectory: `model/`

Model merepresentasikan struktur data dan entitas bisnis dalam sistem.

##### File: `Admin.java`
**Fungsi**: Model untuk data administrator dan staff sistem.

**Atribut Utama**:
```java
public class Admin {
    private int id;
    private String username;
    private String passwordHash;
    private String role; // SUPER_ADMIN, ADMIN, STAFF
    private String fullName;
    private String email;
    private String phone;
}
```

**Kegunaan**: Menyimpan informasi pengguna sistem dengan berbagai level akses.

##### File: `Event.java`
**Fungsi**: Model untuk event/pertandingan di stadion.

**Atribut Utama**:
```java
public class Event {
    private int id;
    private String eventName;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String description;
    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED
    private int totalSeats;
    private int bookedSeats;
}
```

**Kegunaan**: Merepresentasikan event dengan informasi lengkap termasuk status dan kapasitas.

##### File: `Booking.java`
**Fungsi**: Model untuk transaksi pemesanan tiket.

**Atribut Utama**:
```java
public class Booking {
    private int bookingId;
    private int eventId;
    private String bookingNumber;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private int totalSeats;
    private double totalPrice;
    private String bookingStatus; // CONFIRMED, CANCELLED
    private List<BookingSeat> bookingSeats;
}
```

**Cara Kerja**: 
- Setiap booking memiliki nomor unik (format: BK-YYYYMMDD-XXXX)
- Menyimpan relasi dengan kursi yang dipesan melalui `BookingSeat`
- Status dapat berubah dari CONFIRMED ke CANCELLED

##### File: `Section.java`
**Fungsi**: Model untuk seksi/area stadion.

**Atribut Utama**:
```java
public class Section {
    private int sectionId;
    private String sectionName;
    private String sectionType; // TRIBUNE, FIELD
    private int capacity;
    private int rows;
    private int seatsPerRow;
}
```

##### File: `InventoryItem.java`
**Fungsi**: Model untuk item inventaris stadion.

**Atribut Utama**:
```java
public class InventoryItem {
    private int itemId;
    private String itemName;
    private String category;
    private int quantity;
    private int minStockLevel;
    private double unitPrice;
    private String status; // AVAILABLE, LOW_STOCK, OUT_OF_STOCK
}
```

**Kegunaan**: Melacak stok barang dengan sistem peringatan stok rendah.

##### File: `Staff.java`
**Fungsi**: Model untuk data karyawan stadion.

**Atribut Utama**:
```java
public class Staff {
    private int staffId;
    private String name;
    private String position;
    private double salary;
    private String phone;
    private String email;
    private LocalDate hireDate;
    private String status; // ACTIVE, INACTIVE
}
```

##### File: `EventExpense.java`
**Fungsi**: Model untuk pengeluaran per event.

**Atribut Utama**:
```java
public class EventExpense {
    private int expenseId;
    private int eventId;
    private String expenseType; // INVENTORY_DAMAGE, OPERATIONAL
    private int itemId;
    private int quantity;
    private double unitCost;
    private double totalCost;
    private String notes;
}
```

---

#### Subdirectory: `service/`

Service layer mengimplementasikan logika bisnis dan operasi database.

##### File: `BookingService.java`
**Fungsi**: Mengelola seluruh operasi pemesanan tiket.

**Method Utama**:

1. **`createBooking()`**: Membuat pemesanan baru
```java
public Booking createBooking(Booking booking, List<Seat> selectedSeats) {
    Connection conn = null;
    try {
        conn = DatabaseUtil.getConnection();
        conn.setAutoCommit(false); // Mulai transaksi
        
        // Generate nomor booking
        String bookingNumber = generateBookingNumber();
        booking.setBookingNumber(bookingNumber);
        
        // Insert data booking
        // Insert detail kursi
        // Update kapasitas
        
        conn.commit();
        
        // Kirim email konfirmasi (asynchronous)
        sendBookingConfirmationEmailAsync(booking);
        
        return booking;
    } catch (SQLException e) {
        conn.rollback();
        return null;
    }
}
```

**Cara Kerja**:
- Menggunakan transaksi database untuk menjamin konsistensi data
- Generate nomor booking otomatis dengan format BK-YYYYMMDD-XXXX
- Mengurangi kapasitas tersedia setelah booking berhasil
- Mengirim email konfirmasi secara asynchronous

2. **`cancelBooking()`**: Membatalkan pemesanan
```java
public boolean cancelBooking(int bookingId) {
    // Update status booking menjadi CANCELLED
    // Kembalikan kapasitas seksi
    // Kirim email pembatalan
    return true;
}
```

3. **`getAllBookings()`**: Mengambil semua data booking
4. **`getBookingById()`**: Mengambil detail booking termasuk kursi

**Integrasi Email**:
- Menggunakan thread terpisah untuk pengiriman email
- Tidak memblokir proses booking jika email gagal
- Format email HTML dengan desain responsif

##### File: `EventService.java`
**Fungsi**: Mengelola data event/pertandingan.

**Method Utama**:

1. **`createEvent()`**: Membuat event baru
```java
public boolean createEvent(Event event) {
    String query = "INSERT INTO Events (EventName, EventType, EventDate, " +
                  "EventTime, Description, Status, TotalSeats) " +
                  "VALUES (?, ?, ?, ?, ?, ?, ?)";
    // Execute query
    return true;
}
```

2. **`getAllEvents()`**: Mengambil semua event
3. **`updateEvent()`**: Memperbarui informasi event
4. **`deleteEvent()`**: Menghapus event
5. **`getUpcomingEvents()`**: Filter event yang akan datang

**Validasi**:
- Memastikan tanggal event tidak di masa lalu
- Validasi kapasitas total tidak melebihi kapasitas stadion
- Status event dikelola secara otomatis

##### File: `SeatService.java`
**Fungsi**: Mengelola data kursi stadion.

**Method Utama**:

1. **`getSeatsBySection()`**: Mengambil kursi per seksi
```java
public List<Seat> getSeatsBySection(int sectionId) {
    String query = "SELECT * FROM Seats WHERE SectionID = ? ORDER BY RowNumber, SeatNumber";
    // Execute dan return list seats
}
```

2. **`getAvailableSeats()`**: Kursi yang tersedia untuk event tertentu
3. **`updateSeatStatus()`**: Update status kursi (AVAILABLE/BOOKED)

##### File: `AdminService.java`
**Fungsi**: Manajemen administrator dan autentikasi.

**Method Utama**:

1. **`authenticate()`**: Login sistem
```java
public Admin authenticate(String username, String password) {
    String query = "SELECT * FROM Admins WHERE Username = ?";
    // Verify password hash
    // Return admin object if valid
}
```

**Keamanan**:
- Password di-hash menggunakan algoritma yang aman
- Validasi username dan password sebelum query
- Session management untuk maintain login state

2. **`createAdmin()`**: Menambah admin baru
3. **`updateAdmin()`**: Update data admin
4. **`changePassword()`**: Ubah password dengan verifikasi

##### File: `InventoryService.java`
**Fungsi**: Manajemen inventaris stadion.

**Method Utama**:

1. **`getAllItems()`**: Mengambil semua item inventaris
2. **`addItem()`**: Menambah item baru
3. **`updateStock()`**: Update jumlah stok
```java
public boolean updateStock(int itemId, int quantity) {
    String query = "UPDATE InventoryItems SET Quantity = ? WHERE ItemID = ?";
    // Update dan check min stock level
    // Update status jika low stock
}
```

4. **`getLowStockItems()`**: Item dengan stok rendah
5. **`checkStockStatus()`**: Cek dan update status item

**Fitur Monitoring**:
- Otomatis menandai item dengan stok rendah
- Alert untuk item yang perlu di-restock
- Tracking kategori dan harga satuan

##### File: `StaffService.java`
**Fungsi**: Manajemen data karyawan.

**Method Utama**:
1. **`getAllStaff()`**: Daftar semua staff
2. **`addStaff()`**: Tambah karyawan baru
3. **`updateStaff()`**: Update data karyawan
4. **`getActiveStaff()`**: Filter staff aktif

##### File: `ReportService.java`
**Fungsi**: Menghasilkan laporan dan analisis.

**Method Utama**:

1. **`getTotalRevenue()`**: Total pendapatan
```java
public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
    String query = "SELECT SUM(TotalPrice) FROM Bookings " +
                  "WHERE BookingStatus = 'CONFIRMED' " +
                  "AND BookingDate BETWEEN ? AND ?";
}
```

2. **`getSectionPopularity()`**: Popularitas seksi
3. **`getEventPerformance()`**: Performa per event
4. **`getProfitForEvent()`**: Perhitungan profit dengan expense

**Analisis yang Disediakan**:
- Revenue per periode
- Tingkat hunian per seksi
- Event terlaris
- Tren pemesanan
- Profit margin per event

##### File: `EventExpenseService.java`
**Fungsi**: Manajemen pengeluaran event.

**Method Utama**:
1. **`addExpense()`**: Catat pengeluaran
2. **`getExpensesForEvent()`**: Daftar pengeluaran per event
3. **`getTotalExpenses()`**: Total pengeluaran

##### File: `SeatGenerationService.java`
**Fungsi**: Generate kursi otomatis untuk seksi baru.

**Cara Kerja**:
```java
public void generateSeatsForSection(int sectionId, int rows, int seatsPerRow) {
    for (int row = 1; row <= rows; row++) {
        for (int seat = 1; seat <= seatsPerRow; seat++) {
            // Insert seat with naming convention
            // Example: A1, A2, B1, B2, etc.
        }
    }
}
```

---

#### Subdirectory: `controller/`

Controller menangani interaksi pengguna dan koordinasi antara view dengan service.

##### File: `LoginController.java`
**Fungsi**: Mengelola proses login pengguna.

**Method Utama**:

1. **`handleLogin()`**: Proses autentikasi
```java
private void handleLogin(ActionEvent event) {
    String username = usernameField.getText();
    String password = passwordField.getText();
    
    AdminService adminService = new AdminService();
    Admin admin = adminService.authenticate(username, password);
    
    if (admin != null) {
        // Buka dashboard
        openDashboard(admin);
    } else {
        // Tampilkan error
        showError("Username atau password salah");
    }
}
```

**Fitur**:
- Validasi input sebelum query
- Error handling dengan pesan yang jelas
- Transisi ke dashboard setelah login sukses

##### File: `DashboardController.java`
**Fungsi**: Controller utama untuk dashboard dan navigasi.

**Method Utama**:

1. **`showHomePage()`**: Menampilkan halaman utama
```java
private void showHomePage() {
    VBox homePage = new VBox(20);
    homePage.setPadding(new Insets(30));
    
    // Welcome message
    Label welcomeLabel = new Label("Selamat Datang, " + admin.getFullName());
    
    // Statistics cards
    StatsSection statsSection = new StatsSection();
    
    // Quick actions
    HBox quickActions = createQuickActionsBar();
    
    // Recent bookings
    BookingsTableView recentBookings = new BookingsTableView();
    
    homePage.getChildren().addAll(
        welcomeLabel, statsSection, quickActions, recentBookings
    );
    
    contentArea.getChildren().setAll(homePage);
}
```

2. **`showEventsPage()`**: Halaman manajemen event
3. **`showBookingsPage()`**: Halaman pemesanan
4. **`showSeatsPage()`**: Konfigurasi kursi
5. **`showReportsPage()`**: Laporan dan analisis

**Navigasi**:
- Side menu dengan icon FontAwesome
- Content area dinamis berdasarkan menu yang dipilih
- Breadcrumb untuk tracking posisi

**Integrasi Komponen**:
```java
private void setupNavigationHandlers() {
    navigationMenu.setOnHomeClick(() -> showHomePage());
    navigationMenu.setOnEventsClick(() -> showEventsPage());
    navigationMenu.setOnBookingsClick(() -> showBookingsPage());
    navigationMenu.setOnSeatsClick(() -> showSeatsPage());
    navigationMenu.setOnReportsClick(() -> showReportsPage());
}
```

---

#### Subdirectory: `component/`

Component berisi komponen UI yang dapat digunakan kembali.

##### File: `NavigationMenu.java`
**Fungsi**: Menu navigasi sidebar.

**Struktur**:
```java
public class NavigationMenu extends VBox {
    private Button homeButton;
    private Button eventsButton;
    private Button bookingsButton;
    private Button seatsButton;
    private Button reportsButton;
    
    public NavigationMenu() {
        createMenuButtons();
        styleMenu();
    }
    
    private void createMenuButtons() {
        homeButton = createMenuButton("Dashboard", FontAwesomeIcon.HOME);
        eventsButton = createMenuButton("Events", FontAwesomeIcon.CALENDAR);
        // ...
    }
}
```

**Fitur**:
- Icon dari FontAwesome
- Highlight untuk menu aktif
- Event handlers untuk setiap button

##### File: `EventTableView.java`
**Fungsi**: Tabel untuk menampilkan daftar event.

**Kolom Tabel**:
- Nama Event
- Tipe Event
- Tanggal & Waktu
- Status
- Kapasitas (Terisi/Total)
- Aksi (Edit/Delete)

**Implementasi**:
```java
public class EventTableView extends TableView<Event> {
    public EventTableView() {
        setupColumns();
        loadData();
    }
    
    private void setupColumns() {
        TableColumn<Event, String> nameCol = new TableColumn<>("Nama Event");
        nameCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEventName())
        );
        
        // Kolom lainnya...
        
        getColumns().addAll(nameCol, typeCol, dateCol, statusCol, capacityCol, actionCol);
    }
}
```

##### File: `BookingWizardView.java`
**Fungsi**: Wizard multi-step untuk pemesanan tiket.

**Tahapan**:
1. **Pilih Event**: Daftar event yang tersedia
2. **Pilih Seksi**: Seksi dengan harga dan kapasitas
3. **Pilih Kursi**: Visual seat map
4. **Data Pelanggan**: Form input data
5. **Konfirmasi**: Review dan konfirmasi

**Cara Kerja**:
```java
public class BookingWizardView extends VBox {
    private int currentStep = 1;
    private Event selectedEvent;
    private EventSection selectedSection;
    private List<Seat> selectedSeats = new ArrayList<>();
    
    private void nextStep() {
        currentStep++;
        renderCurrentStep();
    }
    
    private void renderCurrentStep() {
        switch(currentStep) {
            case 1: showEventSelection(); break;
            case 2: showSectionSelection(); break;
            case 3: showSeatSelection(); break;
            case 4: showCustomerForm(); break;
            case 5: showConfirmation(); break;
        }
    }
}
```

##### File: `SeatMapGrid.java`
**Fungsi**: Visualisasi peta kursi interaktif.

**Fitur**:
- Grid layout dengan baris dan nomor kursi
- Warna berbeda untuk status (tersedia/terpesan)
- Click untuk select/deselect kursi
- Legenda status kursi

**Implementasi**:
```java
public class SeatMapGrid extends GridPane {
    private EventSection eventSection;
    private List<Seat> selectedSeats;
    
    public void renderSeats(List<Seat> seats) {
        getChildren().clear();
        
        Map<String, List<Seat>> seatsByRow = groupSeatsByRow(seats);
        int rowIndex = 0;
        
        for (Map.Entry<String, List<Seat>> entry : seatsByRow.entrySet()) {
            String row = entry.getKey();
            Label rowLabel = new Label(row);
            add(rowLabel, 0, rowIndex);
            
            int colIndex = 1;
            for (Seat seat : entry.getValue()) {
                SeatButton btn = new SeatButton(seat);
                btn.setOnAction(e -> toggleSeatSelection(seat));
                add(btn, colIndex++, rowIndex);
            }
            rowIndex++;
        }
    }
}
```

##### File: `StatsSection.java`
**Fungsi**: Menampilkan kartu statistik di dashboard.

**Metrik yang Ditampilkan**:
- Total Event Bulan Ini
- Total Booking Hari Ini
- Total Pendapatan
- Tingkat Hunian Rata-rata

**Struktur**:
```java
public class StatsSection extends HBox {
    public StatsSection() {
        setSpacing(20);
        loadStats();
    }
    
    private void loadStats() {
        StatCard eventsCard = new StatCard(
            "Total Events", 
            String.valueOf(totalEvents),
            FontAwesomeIcon.CALENDAR
        );
        
        StatCard bookingsCard = new StatCard(
            "Bookings Hari Ini",
            String.valueOf(todayBookings),
            FontAwesomeIcon.TICKET
        );
        
        // ...
        
        getChildren().addAll(eventsCard, bookingsCard, revenueCard, occupancyCard);
    }
}
```

##### File: `EventFormDialog.java`
**Fungsi**: Dialog untuk create/edit event.

**Form Fields**:
- Nama Event (TextField)
- Tipe Event (ComboBox)
- Tanggal (DatePicker)
- Waktu (TimePicker)
- Deskripsi (TextArea)
- Konfigurasi Seksi (EventSectionSelector)

**Validasi**:
```java
private boolean validateForm() {
    if (nameField.getText().isEmpty()) {
        showError("Nama event harus diisi");
        return false;
    }
    
    if (dateField.getValue().isBefore(LocalDate.now())) {
        showError("Tanggal event tidak boleh di masa lalu");
        return false;
    }
    
    // Validasi lainnya...
    return true;
}
```

##### File: `BookingsTableView.java`
**Fungsi**: Tabel untuk menampilkan daftar booking.

**Kolom**:
- Nomor Booking
- Nama Customer
- Event
- Jumlah Kursi
- Total Harga
- Status
- Aksi (View/Cancel)

##### File: `InventoryView.java`
**Fungsi**: Interface manajemen inventaris.

**Fitur**:
- Daftar item dalam kartu
- Filter berdasarkan kategori dan status
- Indikator low stock
- Form tambah/edit item
- Update stok

##### File: `ReportsMainView.java`
**Fungsi**: Hub untuk berbagai jenis laporan.

**Jenis Laporan**:
1. **Laporan Keuangan** (FinancialReportView)
2. **Laporan Event** (EventReportView)
3. **Laporan Stadion** (StadiumReportView)

##### File: `FinancialReportView.java`
**Fungsi**: Laporan keuangan dan profit.

**Konten**:
- Total Revenue per periode
- Total Expense per periode
- Net Profit
- Grafik tren pendapatan
- Breakdown per event

**Implementasi**:
```java
public class FinancialReportView extends VBox {
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    
    private void generateReport() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        ReportService reportService = new ReportService();
        double revenue = reportService.getTotalRevenue(startDate, endDate);
        double expense = reportService.getTotalExpenses(startDate, endDate);
        double profit = revenue - expense;
        
        updateReportCards(revenue, expense, profit);
        updateChart(startDate, endDate);
    }
}
```

##### File: `StaffManagementView.java`
**Fungsi**: Manajemen data karyawan.

**Fitur**:
- Daftar staff dalam tabel
- Filter berdasarkan posisi dan status
- Form tambah/edit staff
- Update status (Active/Inactive)
- Informasi salary dan hire date

---

#### Subdirectory: `util/`

Utility class untuk fungsi-fungsi pembantu.

##### File: `DatabaseUtil.java`
**Fungsi**: Manajemen koneksi database.

**Method Utama**:

1. **`getConnection()`**: Mendapatkan koneksi database
```java
public static Connection getConnection() throws SQLException {
    Dotenv dotenv = Dotenv.load();
    
    String host = dotenv.get("DB_HOST");
    String port = dotenv.get("DB_PORT");
    String database = dotenv.get("DB_NAME");
    String username = dotenv.get("DB_USER");
    String password = dotenv.get("DB_PASSWORD");
    
    String connectionUrl = String.format(
        "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s",
        host, port, database, encrypt
    );
    
    return DriverManager.getConnection(connectionUrl, username, password);
}
```

**Konfigurasi**:
- Menggunakan connection pooling untuk performa
- Credential disimpan di file `.env` (tidak di-commit ke Git)
- Support untuk SQL Server authentication

2. **`closeConnection()`**: Menutup koneksi dengan aman
3. **`testConnection()`**: Tes koneksi database

**Best Practices**:
- Always close connections di finally block
- Gunakan try-with-resources
- Handle SQLException dengan proper logging

##### File: `MailUtils.java`
**Fungsi**: Utility untuk pengiriman email.

**Method Utama**:

1. **`sendBookingConfirmation()`**: Kirim email konfirmasi booking
```java
public static boolean sendBookingConfirmation(Booking booking, Event event) {
    try {
        Session session = getEmailSession();
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME));
        message.setRecipients(Message.RecipientType.TO, 
            InternetAddress.parse(booking.getCustomerEmail()));
        message.setSubject("Booking Confirmation - " + event.getEventName());
        
        String emailContent = buildBookingConfirmationEmail(booking, event);
        message.setContent(emailContent, "text/html; charset=utf-8");
        
        Transport.send(message);
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}
```

**Template Email**:
- HTML template dengan CSS inline
- Informasi booking lengkap
- Barcode/QR code untuk verifikasi (opsional)
- Responsive design untuk mobile

2. **`sendBookingCancellation()`**: Email pembatalan
3. **`testEmailConfiguration()`**: Tes konfigurasi email

**Konfigurasi SMTP**:
```java
private static Session getEmailSession() {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");
    
    return Session.getInstance(props, new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
        }
    });
}
```

##### File: `IconUtil.java`
**Fungsi**: Helper untuk membuat icon FontAwesome.

**Method**:
```java
public static FontAwesomeIconView createIcon(FontAwesomeIcon icon, int size) {
    FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
    iconView.setSize(String.valueOf(size));
    return iconView;
}

public static FontAwesomeIconView createColoredIcon(
    FontAwesomeIcon icon, int size, Color color
) {
    FontAwesomeIconView iconView = createIcon(icon, size);
    iconView.setFill(color);
    return iconView;
}
```

---

#### File: `App.java`
**Fungsi**: Main application class dan entry point.

**Method Utama**:

1. **`start()`**: Initialize aplikasi
```java
@Override
public void start(Stage stage) {
    try {
        // Load environment variables
        Dotenv dotenv = Dotenv.load();
        
        // Test database connection
        DatabaseUtil.testConnection();
        
        // Show login screen
        LoginController loginController = new LoginController(stage);
        Scene loginScene = loginController.getScene();
        
        stage.setScene(loginScene);
        stage.setTitle("Stadium Management System - Login");
        stage.setMaximized(true);
        stage.show();
        
    } catch (Exception e) {
        showErrorDialog("Error", "Gagal memulai aplikasi: " + e.getMessage());
    }
}
```

2. **`main()`**: Entry point
```java
public static void main(String[] args) {
    launch(args);
}
```

**Initialization Flow**:
1. Load konfigurasi dari `.env`
2. Test koneksi database
3. Initialize JavaFX
4. Tampilkan login screen

---

#### File: `module-info.java`
**Fungsi**: Konfigurasi Java Module System.

**Deklarasi**:
```java
module org.openjfx {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires java.sql;
    requires de.jensd.fx.glyphs.fontawesome;
    requires de.jensd.fx.glyphs.commons;
    requires java.mail;
    requires io.github.cdimascio.dotenv.java;
    
    exports org.openjfx;
    exports org.openjfx.controller;
    exports org.openjfx.model;
    exports org.openjfx.service;
    exports org.openjfx.util;
    exports org.openjfx.component;
    
    opens org.openjfx to de.jensd.fx.fontawesomefx.commons;
}
```

---

## Modul Utama

### 1. Modul Pemesanan Tiket (Booking)

#### Alur Proses Pemesanan:

1. **Staff membuka Booking Wizard**
   ```
   Dashboard → New Booking → BookingWizardView
   ```

2. **Pilih Event**
   - Tampilkan daftar event aktif (status UPCOMING)
   - Filter berdasarkan tanggal dan tipe
   - Menampilkan kapasitas tersedia

3. **Pilih Seksi**
   - Tampilkan seksi yang tersedia untuk event
   - Menampilkan harga per seksi
   - Menampilkan kapasitas tersisa

4. **Pilih Kursi**
   - Visual seat map dengan grid layout
   - Kursi hijau = tersedia, merah = terpesan
   - Multiple selection dengan click
   - Counter jumlah kursi terpilih

5. **Input Data Pelanggan**
   ```java
   CustomerFormPanel:
   - Nama Lengkap (required)
   - Email (required, validated)
   - Nomor Telepon (required)
   ```

6. **Konfirmasi dan Pembayaran**
   - Review detail booking
   - Kalkulasi total harga
   - Konfirmasi pemesanan

7. **Proses Booking**
   ```java
   BookingService.createBooking():
   - Begin transaction
   - Generate booking number
   - Insert booking record
   - Insert booking seats
   - Update section capacity
   - Commit transaction
   - Send confirmation email
   ```

8. **Hasil**
   - Booking berhasil dengan nomor booking
   - Email konfirmasi terkirim ke pelanggan
   - Print receipt (opsional)

#### Validasi Booking:
- Event harus status UPCOMING
- Kapasitas harus mencukupi
- Email harus valid format
- Tidak boleh double booking kursi yang sama

---

### 2. Modul Manajemen Event

#### Fitur Utama:

1. **Create Event**
   ```java
   EventFormDialog:
   - Nama Event
   - Tipe (Football, Concert, etc.)
   - Tanggal dan Waktu
   - Deskripsi
   - Konfigurasi Harga per Seksi
   ```

2. **Edit Event**
   - Update informasi event
   - Ubah konfigurasi harga
   - Update status

3. **Delete Event**
   - Validasi: hanya jika tidak ada booking
   - Konfirmasi sebelum delete

4. **Event Section Configuration**
   ```java
   EventSectionConfigDialog:
   - Pilih seksi aktif
   - Set harga per seksi
   - Set kapasitas available
   ```

#### Status Event Lifecycle:
```
UPCOMING → ONGOING → COMPLETED
    ↓
CANCELLED (bisa dari UPCOMING)
```

---

### 3. Modul Inventaris

#### Fitur:

1. **Manajemen Stok**
   ```java
   InventoryService:
   - Add Item
   - Update Stock
   - Check Stock Level
   - Get Low Stock Items
   ```

2. **Kategori Item**
   - Equipment
   - Supplies
   - Merchandise
   - Food & Beverage
   - Other

3. **Monitoring Stok**
   - Otomatis update status:
     * AVAILABLE (quantity > minStockLevel)
     * LOW_STOCK (quantity ≤ minStockLevel && quantity > 0)
     * OUT_OF_STOCK (quantity = 0)

4. **Purchase Recording**
   ```java
   InventoryPurchaseService:
   - Record pembelian
   - Link ke event (opsional)
   - Tracking total cost
   ```

---

### 4. Modul Laporan

#### Jenis Laporan:

1. **Laporan Keuangan**
   - Total Revenue per periode
   - Total Expense per event
   - Net Profit calculation
   - Revenue trend chart

2. **Laporan Event**
   - Event performance
   - Occupancy rate
   - Revenue per event
   - Expense breakdown

3. **Laporan Stadion**
   - Section popularity
   - Average occupancy
   - Peak usage time
   - Maintenance cost

#### Cara Generate Laporan:
```java
ReportsMainView:
1. Pilih jenis laporan
2. Set periode (start date - end date)
3. Generate Report
4. View in table/chart
5. Export to PDF/Excel (opsional)
```

---

### 5. Modul Administrasi

#### Manajemen User:

1. **Role-Based Access Control**
   ```
   SUPER_ADMIN:
   - Full access
   - Manage admins
   - Manage staff
   - View all reports
   
   ADMIN:
   - Manage events
   - Process bookings
   - Manage inventory
   - View reports
   
   STAFF:
   - Process bookings only
   - View inventory
   - Limited reports
   ```

2. **User Management**
   - Create admin/staff account
   - Update user info
   - Change password
   - Deactivate account

#### Manajemen Staff:

```java
StaffManagementView:
- Add staff dengan posisi dan salary
- Update informasi staff
- Track hire date
- Update status (ACTIVE/INACTIVE)
```

---

## Keamanan Sistem

### Password Hashing
```java
// Password tidak disimpan plain text
String hashedPassword = hashPassword(plainPassword);
```

### SQL Injection Prevention
```java
// Menggunakan PreparedStatement
PreparedStatement stmt = conn.prepareStatement(
    "SELECT * FROM Admins WHERE Username = ?"
);
stmt.setString(1, username);
```

### Environment Variables
```properties
# .env file (not committed to Git)
DB_HOST=localhost
DB_USER=sa
DB_PASSWORD=secure_password
EMAIL_USERNAME=noreply@stadium.com
EMAIL_PASSWORD=app_specific_password
```

---

## Teknologi Integrasi

### Email Service
- **Provider**: Gmail SMTP
- **Protocol**: TLS/SSL
- **Port**: 587
- **Authentication**: App-specific password

**Konfigurasi**:
1. Enable 2-Factor Authentication di Google Account
2. Generate App Password
3. Update `.env` file dengan credentials

### Database Connection
- **Driver**: Microsoft JDBC Driver for SQL Server
- **Connection Pooling**: HikariCP (recommended)
- **Transaction Management**: Manual dengan commit/rollback

---

## Best Practices Implementation

### 1. Transaction Management
```java
Connection conn = null;
try {
    conn = DatabaseUtil.getConnection();
    conn.setAutoCommit(false);
    
    // Multiple operations
    
    conn.commit();
} catch (SQLException e) {
    if (conn != null) conn.rollback();
    throw e;
} finally {
    if (conn != null) conn.close();
}
```

### 2. Resource Management
```java
// Try-with-resources
try (Connection conn = DatabaseUtil.getConnection();
     PreparedStatement stmt = conn.prepareStatement(query);
     ResultSet rs = stmt.executeQuery()) {
    // Process results
} // Auto-close resources
```

### 3. Error Handling
```java
try {
    // Operation
} catch (SQLException e) {
    System.err.println("Database error: " + e.getMessage());
    showErrorDialog("Error", "Gagal menyimpan data");
    e.printStackTrace();
}
```

### 4. Async Operations
```java
// Email sending tidak memblokir UI
new Thread(() -> {
    MailUtils.sendBookingConfirmation(booking, event);
}).start();
```

---

## Deployment

### Prerequisites:
1. Java 11 atau lebih tinggi
2. Microsoft SQL Server 2019+
3. Maven 3.6+

### Build Process:
```bash
# Clean dan compile
mvn clean compile

# Run aplikasi
mvn javafx:run

# Package ke JAR
mvn clean package
```

### Database Setup:
```bash
# Jalankan skrip SQL secara berurutan:
1. 01_initial_setup.sql
2. 02_sync_seats.sql
3. 03_features_roles_staff_inventory.sql
4. 04_add_inventory_fields.sql
5. 06_event_expenses.sql
```

### Environment Configuration:
```properties
# Buat file .env di root project
DB_HOST=localhost
DB_PORT=1433
DB_NAME=StadiumDB
DB_USER=sa
DB_PASSWORD=your_password
EMAIL_USERNAME=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
```

---

## Troubleshooting

### Database Connection Error
**Gejala**: `SQLException: Login failed for user`
**Solusi**:
1. Verifikasi credentials di `.env`
2. Cek SQL Server service running
3. Test connection dengan `DatabaseUtil.testConnection()`

### Email Not Sending
**Gejala**: `AuthenticationFailedException`
**Solusi**:
1. Gunakan App Password, bukan password regular
2. Enable 2FA di Google Account
3. Verifikasi SMTP settings

### Font Icons Not Showing
**Gejala**: Kotak persegi bukan icon
**Solusi**:
1. Run `mvn clean install -U`
2. Reload Maven project
3. Verify fontawesomefx dependency

---

## Kesimpulan

Sistem Manajemen Stadion merupakan solusi komprehensif untuk mengelola operasional stadion secara efisien. Dengan arsitektur MVC yang modular, sistem ini mudah untuk di-maintain dan dikembangkan lebih lanjut. Fitur-fitur utama seperti pemesanan tiket, manajemen inventaris, dan laporan analisis telah terintegrasi dengan baik untuk mendukung kebutuhan bisnis stadion modern.

---

**Versi Dokumentasi**: 1.0.0  
**Tanggal**: 30 November 2025  
**Penyusun**: Stadium Management Development Team
