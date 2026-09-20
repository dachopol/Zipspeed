# Zipspeed by AnakinYoo

Zipspeed is an Android-first network measurement and diagnostics app built with Kotlin + Jetpack Compose.

## Current implementation

The Android app currently includes:

- HTTP-based Download / Upload throughput measurement.
- HTTP latency and jitter measurement.
- A single GO control with live gauge/needle updates and cancel/retry flow.
- Cloudflare Anycast endpoint routing; the app does not pretend that a specific city/PoP was selected unless verifiable endpoint metadata is returned.
- Public/local IP information where available.
- Video-suitability testing based on measured HTTP payload throughput. It is not a real video player.
- Website/CDN HTTP checks with measured response-header timing.
- Local Room history, record sharing, CSV/JSON history export.
- Thai/English UI, dark/light theme and reduced-motion support.
- Responsive navigation for phone, tablet and larger screens.
- Optional GPS mode. Starting a speed test does not require GPS permission.
- External outage-status links. Zipspeed does not fabricate outage counts or live service status.

## Real-data rule

Production UI must not invent network measurements, ISP names, geographic server locations, outage counts, ad impressions, purchase entitlements or security verification. If data cannot be measured or verified, the UI should show an unavailable/unknown state.

## Ads and paid Ad-Free

The UI keeps integration points for ads and Ad-Free, but production AdMob and Google Play Billing are intentionally not enabled until real account configuration is supplied.

Do not ship simulated ads, fixed prices, local entitlement toggles or fake reward callbacks. Prices/currency must come from Google Play, and Ad-Free must be granted only after verified purchase entitlement. Rewarded access must only be granted from a real rewarded-ad callback.

## Build

Prerequisites:

- Android Studio / Android SDK
- JDK 17

CI uses:

```bash
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

The project targets Android API 36 and uses application ID:

```text
com.aistudio.zipspeed.zskt
```

Debug builds use Android's normal debug signing. Release signing expects these environment variables:

```text
KEYSTORE_PATH
STORE_PASSWORD
KEY_PASSWORD
```

The release key alias is currently `upload`.

`.env.example` contains safe placeholder values only. Do not commit real API keys, signing files or passwords.

## Play Store release checks

Before a production release:

1. Run unit tests and build the debug/release artifacts.
2. Test GO/STOP, gauge motion, Download/Upload, latency/jitter, offline/error handling and history on real Android devices.
3. Verify every permission is necessary. GPS must remain optional.
4. Re-check Privacy Policy and Data Safety against the actual code and SDKs used in that release.
5. Configure real AdMob/consent and Google Play Billing before advertising those features.
6. Build a signed AAB, test it in Play Console testing tracks, then verify the installed build.
7. Increase `versionCode` for each Play release.

## Branding

App: **Zipspeed**  
Credit: **by AnakinYoo**
