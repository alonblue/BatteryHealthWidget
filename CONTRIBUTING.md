# Contributing

Thanks for helping improve Pixel Battery Health Quick Settings Tile.

## Development Setup

You need:

- Android Studio or the Android SDK command-line tools
- JDK 21 or newer
- Android SDK Platform 36.1
- Android SDK Build Tools 36.1.0
- A Google Pixel device running Android 15 or newer for real device testing

Clone the repository and run the local checks:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Testing on a Device

Install the debug build:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Grant the required protected permission:

```bash
adb shell pm grant io.github.alonblue.batteryhealthwidget android.permission.WRITE_SECURE_SETTINGS
```

Then add the Charge Limit tile from Android Quick Settings.

## Pull Requests

- Keep changes focused and easy to review.
- Run `./gradlew testDebugUnitTest assembleDebug` before opening a pull request.
- Update the README when behavior, setup steps, device support, or permissions change.
- Avoid committing local Android Studio files, keystores, APKs, or generated build output.

## App ID

The Android application id is:

```text
io.github.alonblue.batteryhealthwidget
```

Changing it creates a separate app install and requires users to grant the ADB permission again.
