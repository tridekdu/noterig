
# Infinite Canvas — Kotlin Multiplatform (Desktop + Android with Onyx)

Tiles live in `segments/` as `x_y_z.png`. New tiles are created only when you draw.

## Build requirements
- JDK 21
- Android SDK for Android build
- Internet access to Boox Maven repo for Onyx SDK

## Desktop run
```bash
./gradlew run
```
A `segments/` folder is created next to where you run. Right-click toggles Pen/Eraser. Middle-drag pans.

## Android deploy
```bash
./gradlew installDebug
adb shell am start -n app.notegamut/.MainActivity
```
Tiles path: `/sdcard/Android/data/app.notegamut/files/segments`. On Onyx devices, the pen SDK is enabled automatically.
