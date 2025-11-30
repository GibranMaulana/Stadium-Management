#!/bin/bash

# =====================================================
# Stadium Management System - Packaging Script
# Creates native Windows installer (.exe)
# =====================================================

echo "========================================"
echo "Stadium Management System - Packager"
echo "========================================"
echo ""

# Check if jpackage is available
if ! command -v jpackage &> /dev/null; then
    echo "❌ ERROR: jpackage not found!"
    echo "   Please install JDK 14+ (jpackage is included)"
    echo "   Current Java version:"
    java -version
    exit 1
fi

echo "✅ jpackage found"
echo ""

# Step 1: Clean and build
echo "📦 Step 1: Building JAR with Maven..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "❌ Maven build failed!"
    exit 1
fi

echo "✅ JAR built successfully"
echo ""

# Step 2: Create app icon (if not exists)
if [ ! -f "icon.ico" ]; then
    echo "⚠️  Warning: icon.ico not found, using default icon"
fi

# Step 3: Create installer directory
echo "📦 Step 2: Preparing jpackage..."
INSTALLER_DIR="target/installer"
rm -rf "$INSTALLER_DIR"
mkdir -p "$INSTALLER_DIR"

# Step 4: Run jpackage
echo "📦 Step 3: Creating Windows installer..."
jpackage \
    --input target \
    --name "Stadium Management System" \
    --main-jar "stadium-management-1.0.0.jar" \
    --main-class org.openjfx.App \
    --type exe \
    --app-version 1.0.0 \
    --vendor "Stadium Management Team" \
    --description "Complete Stadium Management System for ticket booking, inventory, and reporting" \
    --dest "$INSTALLER_DIR" \
    --win-dir-chooser \
    --win-menu \
    --win-shortcut \
    --win-menu-group "Stadium Management" \
    --java-options "-Xmx1024m" \
    --java-options "-Dfile.encoding=UTF-8"

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "✅ SUCCESS!"
    echo "========================================"
    echo ""
    echo "📁 Installer location:"
    echo "   $INSTALLER_DIR/"
    echo ""
    echo "📦 Files created:"
    ls -lh "$INSTALLER_DIR"
    echo ""
    echo "🚀 You can now distribute the .exe installer!"
    echo ""
else
    echo ""
    echo "❌ jpackage failed!"
    echo "   Check error messages above"
    exit 1
fi
