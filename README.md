# Zipspeed by AnakinYoo

Current test-build version: **71.0.0** (`versionCode 71`)  
Android application ID: `com.aistudio.zipspeed.zskt`

Zipspeed is an Android-first network measurement and diagnostics app with a web build that can also be imported into Google AI Studio.

## Canonical project sources

- Android native source: `app/src/main/`
- Android bundled web asset: `app/src/main/assets/index.html`
- Web / AI Studio source: `public/index.html`
- Web build output: `dist/` (generated; not committed)
- Play/source gate: `play-console-check.mjs`

Old root HTML copies, `Zipspeed_Space_v3`, IDE/Gradle caches, temporary patch scripts, screenshots and archived ZIP imports are intentionally removed. Do not restore them as alternate app sources.

## Current implementation

The Android app includes:

- HTTP-based Download / Upload throughput measurement.
- HTTP latency and jitter measurement.
- Single GO/STOP control with live gauge/needle updates and cancel/retry flow.
- Cloudflare Anycast endpoint routing without inventing a city/PoP when metadata is unavailable.
- Public/local IP information where available.
- Video suitability based on measured HTTP throughput; it is not a licensed playback benchmark.
- Website/CDN HTTP checks.
- Local Room history, sharing and CSV/JSON export.
- Thai/English UI, dark/light theme and reduced-motion support.
- Responsive phone/tablet layouts.
- Optional Android GPS mode; normal speed tests do not require GPS.

## Real-data rule

Production UI must not invent network measurements, ISP names, geographic server locations, outage counts, ad impressions, purchase entitlements or security verification. Unknown data must remain unknown/unavailable.

## Web / Google AI Studio

The web project does not require a Gemini API key and the current AI Studio metadata requests no frame permissions or Gemini server capability.

Run locally:

```bash
npm start
```

Build:

```bash
npm run ui:audit
npm run build
npm run play:check
```

For Google AI Studio, import/sync **the `main` branch of `dachopol/Zipspeed`**. Do not continue from an older local AI Studio copy containing removed legacy paths.

## Android build

CI uses JDK 17 and Gradle 9.3.1:

```bash
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The project targets/compiles Android API 36.

Release signing is intentionally conditional. It requires real upload-key secrets and `ENABLE_SIGNED_RELEASE=true` before `:app:bundleRelease` is run.

## Ads and paid Ad-Free

Production AdMob and Google Play Billing are not treated as active until real account configuration and entitlement verification are implemented. Do not ship simulated ads, fixed fake prices or local-only entitlement toggles.

## Release status

Repository/CI source gates can pass without proving live Play Console readiness. Play App Signing, signed-AAB upload acceptance, Privacy Policy, Data Safety, declarations, tester/account requirements and real-device validation remain separate release checks.

See:

- `PLAY_CONSOLE_TEST_GATE.md`
- `PLAY_STORE_RELEASE_AUDIT.md`
- `DATA_SAFETY_WORKSHEET.md`

## Branding

App: **Zipspeed**  
Credit: **by AnakinYoo**
