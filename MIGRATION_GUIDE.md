# Database Migration & .env Setup Guide

## Overview

This guide explains how to set up the database and environment variables for your team.

## Files Created

### 1. `.env.example` - Template for environment variables
```env
DB_HOST=localhost
DB_PORT=1433
DB_NAME=StadiumDB
DB_USER=your_sql_username
DB_PASSWORD=your_sql_password
DB_ENCRYPT=false
```

### 2. `database/01_initial_setup.sql` - Creates all tables
- Admins table
- Sections table (Tribunes & Field Zones)
- Seats table
- Events table
- EventSections table (links events to sections)
- Bookings table
- BookingSeats table (junction table)

### 3. `database/02_sync_seats.sql` - Generates seat records
- Automatically generates seats for all tribune sections
- Based on TotalRows × SeatsPerRow configuration
- Creates row labels (A-Z, then AA-AZ, BA-BZ, etc.)

## Setup Steps for Your Team

### Step 1: Clone Repository

```bash
git clone https://github.com/yourusername/mulet-stadium.git
cd mulet-stadium
```

### Step 2: Create `.env` File

```bash
# Copy the example file
cp .env.example .env

# Edit with your credentials
nano .env  # or use any text editor
```

Example `.env` content:
```env
DB_HOST=localhost
DB_PORT=1433
DB_NAME=StadiumDB
DB_USER=sa
DB_PASSWORD=YourActualPassword123!
DB_ENCRYPT=false
```

**⚠️ IMPORTANT**: Never commit `.env` to Git! It's in `.gitignore`.

### Step 3: Install Maven Dependencies

```bash
mvn clean install
```

This installs:
- JavaFX
- SQL Server JDBC driver
- **dotenv-java** (for reading .env files)
- FontAwesome icons

### Step 4: Setup Database

#### Option A: Fresh Installation (New Database)

```bash
# Run initial setup (creates tables)
sqlcmd -S localhost -U sa -P YourPassword -i database/01_initial_setup.sql

# Generate seats
sqlcmd -S localhost -U sa -P YourPassword -i database/02_sync_seats.sql
```

#### Option B: Using SSMS (SQL Server Management Studio)

1. Open SSMS
2. Connect to your SQL Server
3. Open `database/01_initial_setup.sql`
4. Execute (F5)
5. Open `database/02_sync_seats.sql`
6. Execute (F5)

#### Option C: Existing Database (Already Has Tables)

If your database already exists and has tables:

```bash
# Just sync the seats
sqlcmd -S localhost -U sa -P YourPassword -i database/02_sync_seats.sql
```

### Step 5: Run Application

```bash
mvn javafx:run
```

Login with:
- Username: `admin`
- Password: `admin123`

## How It Works

### 1. Environment Variables (.env)

`DatabaseUtil.java` now reads from `.env`:

```java
// Loads from .env file
private static final String DB_USER = getEnv("DB_USER");
private static final String DB_PASSWORD = getEnv("DB_PASSWORD");
```

**Benefits**:
- ✅ No hardcoded passwords in code
- ✅ Each developer uses their own credentials
- ✅ Easy to change without modifying code
- ✅ Secure (not in Git)

### 2. Migration Scripts

**01_initial_setup.sql** (Run once):
- Creates database if doesn't exist
- Creates all tables
- Inserts default admin user
- Inserts 4 tribune sections (30×50 each)
- Inserts 2 field zones

**02_sync_seats.sql** (Run when needed):
- Generates seat records for tribunes
- Based on section configuration
- Can be re-run safely (deletes old seats first)

## Team Workflow

### When Starting Fresh

```bash
# 1. Clone repo
git clone <repo-url>
cd mulet-stadium

# 2. Setup env
cp .env.example .env
nano .env  # Add your credentials

# 3. Install dependencies
mvn clean install

# 4. Setup database
sqlcmd -S localhost -U sa -P YourPassword -i database/01_initial_setup.sql
sqlcmd -S localhost -U sa -P YourPassword -i database/02_sync_seats.sql

# 5. Run
mvn javafx:run
```

### When Updating Existing Setup

```bash
# Pull latest code
git pull origin main

# Install any new dependencies
mvn clean install

# Run any new migration scripts (if added)
# (Check database/ folder for new scripts)

# Run application
mvn javafx:run
```

## Troubleshooting

### Error: "DB_USER not configured"

**Solution**: Create `.env` file:
```bash
cp .env.example .env
# Then edit .env with your credentials
```

### Error: "Login failed for user"

**Solution**: Check credentials in `.env` file match your SQL Server

### Error: "Database does not exist"

**Solution**: Run `01_initial_setup.sql` first

### Error: "All sections are fully booked"

**Solution**: Run `02_sync_seats.sql` to generate seats

## Database Schema

```
Admins
  ├── AdminID (PK)
  ├── Username
  └── Password

Sections
  ├── SectionID (PK)
  ├── SectionName
  ├── SectionType (TRIBUNE/FIELD)
  ├── TotalRows
  └── SeatsPerRow

Seats
  ├── SeatID (PK)
  ├── SectionID (FK)
  ├── RowNumber
  └── SeatNumber

Events
  ├── EventID (PK)
  ├── EventName
  ├── EventType
  ├── EventDate
  └── EventTime

EventSections
  ├── EventSectionID (PK)
  ├── EventID (FK)
  ├── SectionID (FK)
  ├── Price
  ├── TotalCapacity
  └── AvailableCapacity

Bookings
  ├── BookingID (PK)
  ├── EventID (FK)
  ├── BookingNumber
  ├── CustomerName
  └── TotalSeats

BookingSeats
  ├── BookingSeatID (PK)
  ├── BookingID (FK)
  ├── SeatID (FK)
  ├── RowNumber
  └── SeatNumber
```

## Security Notes

✅ **DO**:
- Keep `.env` file local
- Use strong SQL Server passwords
- Different credentials per environment (dev/prod)

❌ **DON'T**:
- Commit `.env` to Git (already in .gitignore)
- Share passwords in chat/email
- Use production credentials in development
- Hardcode passwords in source files

## Summary

Your team now has:
1. ✅ `.env` for environment configuration
2. ✅ `01_initial_setup.sql` for fresh database setup
3. ✅ `02_sync_seats.sql` for seat generation
4. ✅ `.gitignore` to protect `.env`
5. ✅ `DatabaseUtil.java` integrated with `.env`

When your coworkers clone the repo:
1. Copy `.env.example` → `.env`
2. Fill in their SQL Server credentials
3. Run migration scripts
4. Start coding! 🚀
