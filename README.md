# NEX FOOTBALL

Mobile web app for local football - teams, tournaments, challenges and matches.

**Live site:** https://shayankrmahato4-star.github.io/nex-football/

Open it on a phone and use Chrome menu -> **Add to Home screen** to install it like an app (works offline).

## How it is published

The app itself is stored in the Supabase backend. The workflow in
`.github/workflows/publish.yml` pulls the newest version and publishes it here:

- `index.html` - the app (mobile-first, viewport meta, PWA tags)
- `manifest.webmanifest` - install metadata
- `sw.js` - offline service worker
- `icons/` - app icons (built from the app logo)

To publish a new version: **Actions -> Publish site -> Run workflow**.

## Login

Demo mobile login: any 10-digit phone number, OTP `123456`.
