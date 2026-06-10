# Pixel Battery Health Quick Settings Tile

A lightweight Android utility for Google Pixel devices that adds a Quick Settings
tile to cycle through three charging modes: **Unrestricted**, **80% Limit**, and
**Adaptive Charging**.

> This app changes protected Android system settings. It requires a one-time ADB
> permission grant after installation.

## Features

- **3-state tile**: Cycle modes directly from the Quick Settings shade.
- **Real-time wattage**: When charging, the tile icon and subtitle update every 2 seconds with the current charging wattage.
- **Dynamic icons**: The tile icon and label change based on charging state and selected mode.
- **Native Pixel settings**: Writes `charge_optimization_mode` and `adaptive_charging_enabled`.

## Requirements

- Google Pixel device running Android 15 or newer.
- ADB installed on your computer for the initial permission grant.
- Android Studio or the Android SDK command-line tools if building from source.

The Android application id is:

```text
io.github.alonblue.batteryhealthwidget
```

## Build From Source

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install

Install the debug APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Grant the required protected permission:

```bash
adb shell pm grant io.github.alonblue.batteryhealthwidget android.permission.WRITE_SECURE_SETTINGS
```

On Windows PowerShell, use:

```powershell
& adb shell pm grant io.github.alonblue.batteryhealthwidget android.permission.WRITE_SECURE_SETTINGS
```

If `adb` is not in your PATH, use the full path to the Android SDK
`platform-tools` directory.

## Add the Tile

1. Swipe down twice to open the full Quick Settings panel.
2. Tap the edit pencil.
3. Find the **Charge Limit** tile and drag it into your active tiles.

## Charging Modes

- **80% Limit**: Caps charging at 80% to prolong long-term battery health.
- **Adaptive**: Lets Android finish charging close to when you usually unplug.
- **Unrestricted**: Standard charging to 100%.

## Uninstall or Reset Permission

Revoke the protected permission:

```bash
adb shell pm revoke io.github.alonblue.batteryhealthwidget android.permission.WRITE_SECURE_SETTINGS
```

Uninstall the app:

```bash
adb uninstall io.github.alonblue.batteryhealthwidget
```

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md)
before opening a pull request.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
