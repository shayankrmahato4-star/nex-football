# NEX FOOTBALL

Local football, organised — teams, tournaments, challenges, matches and end-to-end encrypted messaging.

This repository holds **all** the NEX FOOTBALL code:

| Folder | What it is |
|---|---|
| `web/` | The web app (single-page HTML/CSS/JS). Runs in the browser and as the PWA. |
| `android/` | The native Android app (Kotlin). |
| `releases/` | Signed APK builds. |
| `docs/` | Conversion + validation reports. |
| `index.html` | The published build served by GitHub Pages (written by the publish workflow). |

Live web app: https://shayankrmahato4-star.github.io/nex-football/

## Web app (`web/index.html`)

A single self-contained file — no build step. Open it in a browser, or serve the folder.
It talks to Supabase (auth, Postgres with row-level security, storage, realtime).

Features: demo phone + OTP login, home, tournaments (create, edit, manage, brackets, payments),
teams (squads, roles, join requests), challenges (amount, date, time via clock picker, ground,
rules, duration), matches, discovery, profiles (photo, attributes, once-a-week location change),
notifications, admin, and **Messages** — one-to-one chat with client-side E2EE.

## Android app (`android/`)

Native Kotlin app (package `com.nex.football`, minSdk 24, targetSdk 34).

* Every screen is the **same UI file** rendered in the platform WebView, served from a bundled
  asset over a loopback HTTP origin — so the design is preserved 1:1 instead of re-drawn.
* Kotlin adds what the browser cannot do: **Android Keystore** storage for the E2EE private key,
  camera/photo upload, microphone permission, share, hardware back button, FCM push hooks.

Build with Android Studio / Gradle, or with the plain toolchain (`aapt2` + `kotlinc` + `d8` +
`zipalign` + `apksigner`) — the project has no third-party dependencies.

### Push notifications
`PushService.kt` is a template. To activate: create a Firebase project, add
`android/app/google-services.json`, uncomment the Firebase dependencies and the
`com.google.gms.google-services` plugin in `android/app/build.gradle.kts`, and uncomment the
`<service>` entry in `AndroidManifest.xml`. Payloads must stay generic — no plaintext in push.

## Messaging security

Messages are encrypted on the device (ECDH P-256 key agreement + AES-GCM, WebCrypto) and the
server stores **ciphertext and IV only**. Private keys never leave the device; in the Android app
they are held in hardware-backed storage.

**Honest limits:** this is genuine E2EE built on reviewed primitives, but it is **not** the Signal
Protocol — there is no forward secrecy or deniability yet. Voice calls are **not** implemented
(WebRTC + microphone grants + a TURN relay are required), and encrypted attachments are an
architecture stub only.

## Backend

Supabase: Auth, Postgres (JSON-row app tables plus the messaging tables `conversations`,
`conversation_participants`, `messages`, `chat_keys`, `device_tokens`, `call_sessions`,
`call_signals`), row-level security on every table, Storage, Realtime.

Only the **publishable** anon key appears in client code. No service-role or secret keys are in
this repository.

## Repo workflow

`.github/workflows/publish.yml` fetches the latest app build and commits it as `index.html`,
which GitHub Pages serves.
