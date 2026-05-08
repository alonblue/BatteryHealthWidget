# Pixel Battery Health Quick Settings Tile

A lightweight Android utility designed for Google Pixel devices that adds a Quick Settings tile to cycle through three battery charging modes: **Unrestricted**, **80% Limit**, and **Adaptive Charging**.

## 🚀 Features
- **3-State Toggle**: Cycle through modes directly from your notification shade.
- **Real-time Wattage Display**: When charging, the tile icon and subtitle dynamically update every 2 seconds to show the current charging wattage (W).
- **Dynamic Icons**: The tile icon and label change based on the active mode and charging status.
- **Native Integration**: Modifies the actual `charge_optimization_mode` and `adaptive_charging_enabled` system settings.

## 🛠️ Setup Instructions

### 1. Install the App
Build and install the APK onto your Google Pixel device.

### 2. Grant Permissions (Required)
Because modifying system battery settings is a protected action, you must manually grant the `WRITE_SECURE_SETTINGS` permission via ADB. 

Connect your phone to your computer and run:

**Windows (Command Prompt):**
```cmd
adb shell pm grant com.example.batteryhealthwidget android.permission.WRITE_SECURE_SETTINGS
```

**Windows (PowerShell):**
```powershell
& adb shell pm grant com.example.batteryhealthwidget android.permission.WRITE_SECURE_SETTINGS
```

*Note: If `adb` is not in your PATH, use the full path to `adb.exe` located in your Android SDK `platform-tools` folder.*

### 3. Add the Tile
1. Swipe down twice to open the full Quick Settings panel.
2. Tap the **Edit** (pencil) icon.
3. Find the **Charge Limit** tile and drag it into your active tiles.

## 📱 Charging Modes
- **80% Limit**: Caps charging at 80% to prolong long-term battery health.
- **Adaptive**: Learns your routine to finish charging just before you unplug.
- **Unrestricted**: Standard charging to 100% at full speed.

## ⚖️ Requirements
- Google Pixel device (running Android 14+)
- ADB installed on your computer for the initial setup.
