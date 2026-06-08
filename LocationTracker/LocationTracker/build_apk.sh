#!/bin/bash
# ============================================================
#  Location Tracker — Quick Build Script
#  Run this from the LocationTracker/ project root directory
# ============================================================

echo "📍 Location Tracker Build Script"
echo "================================="

# Check Java
if ! command -v java &>/dev/null; then
    echo "❌ Java not found. Install JDK 17: https://adoptium.net"
    exit 1
fi

echo "✅ Java: $(java -version 2>&1 | head -1)"

# Check ANDROID_HOME
if [ -z "$ANDROID_HOME" ]; then
    echo ""
    echo "⚠️  ANDROID_HOME is not set."
    echo "   Set it to your Android SDK location, e.g.:"
    echo "   export ANDROID_HOME=~/Android/Sdk  (Linux)"
    echo "   export ANDROID_HOME=~/Library/Android/sdk  (macOS)"
    echo ""
fi

# Make gradlew executable
chmod +x gradlew

echo ""
echo "🔨 Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "✅ Build successful!"
    echo "📦 APK: $APK_PATH"
    echo ""
    echo "To install on connected device:"
    echo "  adb install $APK_PATH"
else
    echo ""
    echo "❌ Build failed. Check errors above."
    echo "Common fixes:"
    echo "  - Add Google Maps API key to AndroidManifest.xml"
    echo "  - Run: ./gradlew dependencies  (to check dependencies)"
    echo "  - Open in Android Studio for detailed error messages"
fi
