# NEX FOOTBALL - Web to Kotlin Native Conversion Report

## Source and target
- **Source:** the single-page HTML/CSS/JS app (`index.html`, ~784 KB, ~45 screens) that also
  runs as the web app and the PWA.
- **Target:** Kotlin Android app - package `com.nex.football`, minSdk 24, targetSdk 34,
  compileSdk 34, signed APK.

## Approach (and why)
The skill's priority order is functionality > design > frontend behaviour > backend. To hold
**every** screen, colour, animation and interaction identical rather than re-drawing ~45 screens
approximately, the app is a **native Kotlin host** that renders the **same UI file** in the
platform WebView, served from a bundled asset over a loopback HTTP origin (`AssetServer.kt`,
`http://127.0.0.1:<port>/`) so web storage, fetch and PWA behaviour match the browser build.

This is a deliberate trade-off, stated plainly: **the UI is not re-implemented in Compose/Views.**
A screen-by-screen Compose rewrite is a separate, larger task and would risk visual drift.

## Files created
| File | Purpose |
|---|---|
| `app/src/main/java/com/nex/football/MainActivity.kt` | Native host: WebView hardening, file/photo upload, camera+mic permission grants, back-button handling |
| `app/src/main/java/com/nex/football/AssetServer.kt` | Loopback HTTP server serving bundled assets |
| `app/src/main/java/com/nex/football/NexBridge.kt` | JS bridge: Keystore storage, share, toast, permissions, FCM token, device info |
| `app/src/main/java/com/nex/football/KeyVault.kt` | AES-GCM with a hardware-backed Android Keystore key |
| `app/src/main/java/com/nex/football/PushService.kt` | FCM template (inactive until Firebase is configured) |
| `app/src/main/AndroidManifest.xml` | Permissions, launcher activity |
| `app/src/main/assets/index.html` | The app UI (identical to the web build) |
| `app/build.gradle.kts`, `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties` | Gradle project (optional path) |
| `app/src/main/res/**` | Icons (5 densities), theme, strings |
| `README.md` | Build and configuration notes |

## Features preserved (all still functional)
Login (demo phone + OTP), onboarding, home, tournaments list + details + create/edit + manage +
bracket, teams (create, squad, roles, join requests, leave), challenges (sheet, rules, duration,
clock picker), matches, discovery, profile (photo, attributes, weekly location limit), messages
conversation list + chat thread with E2EE, notifications, payments, admin, search, image uploads,
animations, loading/empty/error states, back navigation.

## Modified for native
- E2EE private key now prefers **Android Keystore** (`NexBridge.secureSet/secureGet`) and falls
  back to localStorage only in a plain browser.
- Hardware **back button** drives the app's own navigation stack (`NEX_CAN_BACK` / `NEX_GO_BACK`).
- FCM token is read from the shell and registered against the signed-in player.

## Backend (unchanged endpoints)
Supabase Auth, tables with RLS, Storage, Realtime. Only the publishable anon key is in the client -
no service-role or secret keys.

## Not converted 1:1 (marked, not hidden)
| Item | Status |
|---|---|
| UI in Compose/Views | Not done - rendered by WebView to guarantee parity |
| FCM push | Code + steps included, **inactive** until you add your own `google-services.json` |
| Voice calls | Not implemented (needs mic grants + a TURN relay) |
| Encrypted attachments | Architecture stub only |

## Permissions
INTERNET, ACCESS_NETWORK_STATE, CAMERA, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS, VIBRATE,
POST_NOTIFICATIONS.

## Dependencies
None beyond the Android platform (so it builds with `aapt2` + `kotlinc` + `d8`).
Firebase Messaging is optional and documented.
