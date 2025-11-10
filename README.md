# Stadium Management System

A JavaFX-based desktop application for managing stadium events, seat bookings, and customer transactions.

## Features (Planned)
- 🔐 Admin authentication system
- 📅 Event management (football matches, concerts, etc.)
- 💺 Seat selection and booking
- 👥 Customer management at cashier
- 💰 Payment processing
- 📊 Reports and analytics

## Technology Stack
- **Frontend**: JavaFX 25
- **Backend**: Java 11
- **Database**: Microsoft SQL Server
- **Build Tool**: Maven

## Prerequisites
1. **Java Development Kit (JDK) 11 or higher**
2. **Apache Maven** (for building the project)
3. **Microsoft SQL Server** (Express or higher)
4. **SQL Server Management Studio (SSMS)** or Azure Data Studio (optional, for database management)

## Database Setup

### Step 1: Install SQL Server
If you don't have SQL Server installed:
- Download [SQL Server Express](https://www.microsoft.com/en-us/sql-server/sql-server-downloads)
- During installation, note your server name (usually `localhost` or `.\SQLEXPRESS`)
- Set up SQL Server Authentication with a username (e.g., `sa`) and strong password

### Step 2: Create the Database
1. Open SQL Server Management Studio or Azure Data Studio
2. Connect to your SQL Server instance
3. Open the file `database/setup.sql`
4. Execute the script to create the database and tables

The script will:
- Create a database named `StadiumDB`
- Create tables: `Admins`, `Events`, `Seats`, `Bookings`
- Insert a default admin user (username: `admin`, password: `admin123`)

### Step 3: Configure Database Connection
Edit the file `src/main/java/org/openjfx/util/DatabaseUtil.java` and update the database connection details:

```java
private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=StadiumDB;encrypt=false;trustServerCertificate=true";
private static final String DB_USER = "sa";  // Your SQL Server username
private static final String DB_PASSWORD = "YourPassword123!";  // Your SQL Server password
```

**Common SQL Server configurations:**
- Default instance: `jdbc:sqlserver://localhost:1433;databaseName=StadiumDB;...`
- Named instance: `jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=StadiumDB;...`
- Remote server: `jdbc:sqlserver://your-server-name:1433;databaseName=StadiumDB;...`

## Running the Application

### Using Maven (Command Line)
```bash
# Clean and compile the project
mvn clean compile

# Run the application
mvn javafx:run
```

### Using VS Code
1. Open the project in VS Code
2. Install the "Extension Pack for Java" if not already installed
3. Press `F5` or use the Run menu to start debugging

## Default Login Credentials
- **Username**: `admin`
- **Password**: `admin123`

⚠️ **Important**: Change the default password in production!

## Project Structure
```
mulet-stadium/
├── src/
│   └── main/
│       └── java/
│           └── org/openjfx/
│               ├── App.java                    # Main application entry point
│               ├── SystemInfo.java             # System utilities
│               ├── controller/
│               │   └── LoginController.java    # Login page controller
│               ├── model/
│               │   └── Admin.java              # Admin model
│               ├── service/
│               │   └── AdminService.java       # Admin business logic
│               └── util/
│                   └── DatabaseUtil.java       # Database connection utility
├── database/
│   └── setup.sql                               # Database setup script
├── pom.xml                                     # Maven configuration
└── README.md                                   # This file
```

## Development Roadmap

### Phase 1: Authentication ✅ (Current)
- [x] Database setup
- [x] Admin login page
- [x] Basic authentication

### Phase 2: Dashboard (Next)
- [ ] Main dashboard layout
- [ ] Navigation menu
- [ ] Admin profile management

### Phase 3: Event Management
- [ ] Create/Edit/Delete events
- [ ] Event listing and search
- [ ] Event status management

### Phase 4: Seat Management
- [ ] Seat layout visualization
- [ ] Seat pricing configuration
- [ ] Real-time seat availability

### Phase 5: Booking System
- [ ] Customer information capture
- [ ] Seat selection interface
- [ ] Booking confirmation
- [ ] Payment processing

### Phase 6: Reporting
- [ ] Sales reports
- [ ] Event statistics
- [ ] Revenue analytics

## Security Notes
⚠️ **Current Implementation**:
- Passwords are stored in plain text (NOT SECURE)
- No password hashing or encryption

🔒 **For Production**:
- Implement password hashing (e.g., BCrypt, PBKDF2)
- Add session management
- Implement role-based access control
- Use prepared statements (already implemented)
- Add input validation and sanitization
- Implement audit logging

## Troubleshooting

### Database Connection Issues
1. **Cannot connect to SQL Server**
   - Verify SQL Server is running: Open SQL Server Configuration Manager
   - Check if TCP/IP is enabled for SQL Server
   - Verify firewall allows connections on port 1433
   - Test connection using SSMS first

2. **Login failed for user**
   - Verify SQL Server Authentication is enabled (not just Windows Authentication)
   - Check username and password in `DatabaseUtil.java`
   - Ensure the user has access to the `StadiumDB` database

3. **Database does not exist**
   - Run the `database/setup.sql` script
   - Verify the database name matches in both script and `DatabaseUtil.java`

### Build Issues
1. **JavaFX modules not found**
   - Ensure Maven dependencies are downloaded: `mvn dependency:resolve`
   - Check internet connection for Maven central repository access

2. **Compilation errors**
   - Clean the project: `mvn clean`
   - Rebuild: `mvn compile`

## Contributing
This project is under active development. Future enhancements and features will be added incrementally.

## License
[Specify your license here]

## Contact
[Your contact information]
