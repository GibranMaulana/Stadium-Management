@echo off
REM =====================================================
REM Stadium Management System - Packaging Script (Windows)
REM Creates native Windows installer (.exe)
REM =====================================================

echo ========================================
echo Stadium Management System - Packager
echo ========================================
echo.

REM Check if jpackage is available
where jpackage >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: jpackage not found!
    echo Please install JDK 14+ jpackage is included
    echo Current Java version:
    java -version
    exit /b 1
)

echo [OK] jpackage found
echo.

REM Step 1: Clean and build
echo [Step 1] Building JAR with Maven...
call mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven build failed!
    exit /b 1
)

echo [OK] JAR built successfully
echo.

REM Step 2: Check for icon
if not exist "icon.ico" (
    echo [Warning] icon.ico not found, using default icon
)

REM Step 3: Create installer directory
echo [Step 2] Preparing jpackage...
set INSTALLER_DIR=target\installer
if exist "%INSTALLER_DIR%" rmdir /s /q "%INSTALLER_DIR%"
mkdir "%INSTALLER_DIR%"

REM Step 4: Run jpackage
echo [Step 3] Creating Windows installer...
jpackage ^
    --input target ^
    --name "Stadium Management System" ^
    --main-jar "stadium-management-1.0.0.jar" ^
    --main-class org.openjfx.App ^
    --type exe ^
    --app-version 1.0.0 ^
    --vendor "Stadium Management Team" ^
    --description "Complete Stadium Management System for ticket booking, inventory, and reporting" ^
    --dest "%INSTALLER_DIR%" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-menu-group "Stadium Management" ^
    --java-options "-Xmx1024m" ^
    --java-options "-Dfile.encoding=UTF-8"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS!
    echo ========================================
    echo.
    echo Installer location:
    echo   %INSTALLER_DIR%\
    echo.
    echo Files created:
    dir "%INSTALLER_DIR%"
    echo.
    echo You can now distribute the .exe installer!
    echo.
) else (
    echo.
    echo ERROR: jpackage failed!
    echo Check error messages above
    exit /b 1
)

pause
