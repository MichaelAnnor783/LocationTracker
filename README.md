# 📍 Location Tracker — Android App

A full-featured Android GPS location tracking app with real-time updates, map visualization, history log, CSV export, and a persistent background foreground service.

---

## ✨ Features

| Feature | Details |
|---|---|
| 🛰️ GPS Tracking | Fused Location Provider (best accuracy) |
| 🔔 Foreground Service | Persistent notification, survives app close |
| 🗺️ Map View | Google Maps with route polyline + start/end markers |
| 📋 History Log | Scrollable list of all recorded points |
| 📤 CSV Export | Share location data via any app |
| ⚙️ Settings | Configurable update interval (5s → 5min) |
| 🔋 Boot Receiver | Auto-restarts tracking after reboot |
| 💾 SQLite Database | All data stored locally on device |

---

## 🏗️ Build Instructions

### Prerequisites
- **Android Studio** (Hedgehog 2023.1.1 or newer): https://developer.android.com/studio
- **JDK 17** (bundled with Android Studio)
- **Android SDK** (API 34 / Android 14) — install via SDK Manager in Android Studio
- **Google Maps API Key** (free tier available)

### Step 1: Get a Google Maps API Key

1. Go to https://console.cloud.google.com
2. Create a new project (or use existing)
3. Enable **Maps SDK for Android**
4. Go to **Credentials → Create Credentials → API Key**
5. Copy the key

### Step 2: Add API Key to the Project

Open `app/src/main/AndroidManifest.xml` and find this comment placeholder:

```xml
<!-- ADD YOUR MAPS API KEY HERE -->
```

Add this inside the `<application>` tag:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### Step 3: Open in Android Studio

1. Launch Android Studio
2. **File → Open** → select the `LocationTracker/` folder
3. Wait for Gradle sync to complete (downloads ~200MB dependencies)
4. If prompted, accept any SDK/tool install prompts

### Step 4: Build the APK

#### Option A — Debug APK (for testing)
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

#### Option B — Release APK (for distribution)
```
Build → Generate Signed Bundle / APK
```
- Choose **APK**
- Create or use an existing **keystore**
- Fill in keystore details
- Choose **release** build variant
- Click Finish

APK location: `app/build/outputs/apk/release/app-release.apk`

#### Option C — Command Line
```bash
# On macOS/Linux:
./gradlew assembleDebug

# On Windows:
gradlew.bat assembleDebug
```

---

## 📲 Install the APK on your Phone

1. Enable **Developer Options** on your Android device:
   - Settings → About Phone → tap "Build Number" 7 times
2. Enable **USB Debugging**: Settings → Developer Options → USB Debugging
3. Connect phone via USB
4. In Android Studio: **Run → Run 'app'** (or click ▶ green button)

OR copy the APK to your phone and tap it to install (need "Install from Unknown Sources" enabled).

---

## 📁 Project Structure

```
LocationTracker/
├── app/
│   ├── build.gradle                    # App dependencies & SDK versions
│   └── src/main/
│       ├── AndroidManifest.xml         # Permissions & component registry
│       ├── java/com/locationtracker/app/
│       │   ├── MainActivity.java       # Main UI + permission handling
│       │   ├── LocationTrackingService.java  # Foreground GPS service
│       │   ├── MapActivity.java        # Google Maps with route
│       │   ├── SettingsActivity.java   # Update interval settings
│       │   ├── DatabaseHelper.java     # SQLite storage + CSV export
│       │   ├── LocationAdapter.java    # RecyclerView list adapter
│       │   ├── LocationEntry.java      # Data model
│       │   ├── AppPreferences.java     # SharedPreferences wrapper
│       │   └── BootReceiver.java       # Auto-start on reboot
│       └── res/
│           ├── layout/                 # XML UI layouts
│           ├── values/                 # Colors, strings, themes
│           ├── drawable/               # Circle background drawable
│           └── menu/                   # Toolbar menu
├── build.gradle                        # Root Gradle config
├── settings.gradle
└── gradle.properties
```

---

## 🔐 Permissions Used

| Permission | Why |
|---|---|
| `ACCESS_FINE_LOCATION` | GPS accuracy |
| `ACCESS_COARSE_LOCATION` | Fallback network location |
| `ACCESS_BACKGROUND_LOCATION` | Tracking while screen off (Android 10+) |
| `FOREGROUND_SERVICE` | Keep service alive |
| `FOREGROUND_SERVICE_LOCATION` | Android 14+ foreground service type |
| `RECEIVE_BOOT_COMPLETED` | Auto-restart after reboot |
| `INTERNET` | Google Maps tiles |
| `WRITE_EXTERNAL_STORAGE` | CSV export (Android ≤9) |

---

## 🛠️ Customization

- **Update interval**: Tap ⋮ menu → Settings (5s / 10s / 30s / 1min / 5min)
- **Map type**: Modify `MapActivity.java` — change `MAP_TYPE_NORMAL` to `MAP_TYPE_SATELLITE` or `MAP_TYPE_HYBRID`
- **Tracking auto-start on boot**: Controlled by `BootReceiver.java`

---

## 📋 Minimum Requirements

- Android 5.0 (API 21) or higher
- GPS hardware
- Google Play Services
