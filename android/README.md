# NEX FOOTBALL - Kotlin Android app

Native Android (Kotlin) build of the NEX FOOTBALL app. The web app stays available
exactly as before; this project is the Android app.

## What is native
- Kotlin + AndroidX, `MainActivity` hosts the app UI.
- `NexBridge.kt` - JS bridge: Android Keystore secret storage, share, toasts,
  runtime permissions (mic/camera/notifications), FCM token.
- `KeyVault.kt` - AES-GCM encryption with a hardware-backed Keystore key. The E2EE
  private key is stored here, never in plain preferences.
- `PushService.kt` - Firebase Cloud Messaging (generic payloads only).
- Camera/photo upload, file chooser, back-button handling, permission requests.

## Why the UI is the same
The screens are rendered from `app/src/main/assets/index.html`, which is the same
UI that runs today. That guarantees the design, layout, animations and every
interaction stay identical rather than being re-drawn approximately.

## Build
1. Android Studio (or `./gradlew assembleDebug`).
2. For push: create a Firebase project, add `app/google-services.json`, then enable
   the `com.google.gms.google-services` plugin in `app/build.gradle.kts`.
3. `./gradlew assembleRelease` (sign with your keystore).

## Permissions
INTERNET, ACCESS_NETWORK_STATE, CAMERA, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS,
VIBRATE, POST_NOTIFICATIONS.

## Updating the UI
Replace `app/src/main/assets/index.html` with the latest app build. Nothing else changes.
