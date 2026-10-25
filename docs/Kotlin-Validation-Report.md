# NEX FOOTBALL - Kotlin Conversion Validation Report

## Build status - PASSED (actually executed)
| Step | Tool | Result |
|---|---|---|
| Resource compile | `aapt2 compile` | OK |
| Resource link + assets | `aapt2 link` | OK |
| Kotlin compile | `kotlinc` (JDK 17) | OK - no errors |
| Dex | `d8 --min-api 24` | OK - `classes.dex` 2,265,088 bytes (Kotlin stdlib included) |
| Align | `zipalign -p 4` | OK |
| Sign | `apksigner` | OK - v1/v2, CN=NEX Football |

**APK:** `NEX-Football-Native.apk`, 1,238,405 bytes.

### Fix applied after the first build
The first APK crashed on launch: the dex was built from the app classes only, so the
**Kotlin standard library was missing** (`NoClassDefFoundError: kotlin/jvm/internal/Intrinsics`).
The build now links `kotlin-stdlib.jar` into the dex (22 KB -> 2.27 MB) and the class list is
verified in the dex (`MainActivity`, `AssetServer`, `NexBridge`, `kotlin/jvm/internal/Intrinsics`).
Verified contents: `AndroidManifest.xml` (package `com.nex.football`, minSdk 24, targetSdk 34),
launcher icons in 5 densities, `assets/index.html` 784,074 bytes, `classes.dex`.

## UI parity - PASSED (by construction)
The app renders the same `index.html` that ships as the web app, so layout, colours, fonts,
spacing, icons, animations and every screen are identical rather than approximated.

## Feature parity - COMPILED, DEVICE TEST PENDING
All native features are compiled into the APK. Runtime behaviour was **not** exercised because
no Android device or emulator is available in this environment. Not claimed as tested:
- app launch and rendering
- hardware back button
- photo/file upload
- permission prompts (mic/camera/notifications)
- Keystore key storage round-trip
- FCM (inactive until Firebase is configured)

## Remaining issues
0. Runtime still not exercised here (no device/emulator) - if anything still fails on launch,
   `MainActivity` now reports load errors on screen instead of a blank page, and falls back to the
   hosted app URL if the bundled asset server cannot start.
1. FCM requires your `google-services.json` + enabling the Gradle plugin and the `<service>` entry.
2. Voice calls are not implemented.
3. Update the app by bumping `versionCode` and re-signing with the same keystore.
4. The web app is untouched and still live - both builds stay available in parallel.
