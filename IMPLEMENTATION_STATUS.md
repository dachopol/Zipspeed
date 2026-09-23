# Zipspeed v71 — Current implementation status

Updated: 2026-09-23  
Version: **71.0.0** (`versionCode 71`)  
Application ID: `com.aistudio.zipspeed.zskt`

## Verified in repository CI

- Web build: PASS.
- Canonical UI audit: PASS when current entry points satisfy the audit.
- Play/source gate: PASS when version/package/API/CI/source-hygiene checks pass.
- Android unit tests: PASS in the v71 CI workflow.
- Android `lintDebug`: PASS in the v71 CI workflow.
- Android `assembleDebug`: PASS in the v71 CI workflow.
- Debug APK artifact generation: PASS in the v71 CI workflow.

## Current source structure

- Web/AI Studio source: `public/index.html`.
- Android native source: `app/src/main/`.
- Android bundled web asset: `app/src/main/assets/index.html`.
- Legacy alternate project copies and generated IDE/Gradle caches are removed.

## Permanent project policy

- `PROJECT_WORKFLOW.md` is the required Zipspeed work order.
- `npm run project:policy` blocks legacy sources, package/version drift and prohibited fake/random/hardcoded runtime measurement patterns.
- Clean source ZIP delivery runs only after policy, web, Play/source and Android debug gates pass.
- Experience Assessment no longer substitutes fabricated Download/Upload/Latency values when a test has no measurements; unavailable ratings remain `--`.

## Implemented behavior

- Single GO/STOP speed-test control and active-job duplicate-start protection.
- Gauge/needle follows measured values rather than fabricated values.
- HTTP Download/Upload throughput.
- HTTP latency and jitter.
- Unknown metrics remain unknown instead of receiving fake defaults.
- Cloudflare Anycast routing without invented city selection.
- Local history/share/export.
- Thai/English, dark/light and responsive layouts.
- Optional Android GPS flow; normal speed tests do not require GPS.
- Web build does not require Gemini API access.

## Still TO VERIFY

- Real Android device behavior: GO/STOP touch, gauge animation, network switching, offline/error recovery, rotation/safe areas, Thai rendering and accessibility.
- Accuracy/behavior under real networks beyond CI fixtures/build checks.
- Play App Signing and upload-key ownership.
- Signed release AAB generation with the real key.
- Acceptance of the signed AAB by the live Play Console app.
- Privacy Policy, Data Safety and Play Console declarations against the final artifact.
- Production AdMob and Play Billing configuration/entitlement flow if monetization is enabled.
- Closed-test/account requirements and package-registration state in the live Play Console.

A green repository CI run is evidence for source/build checks only; it is not a substitute for live-device and live-Play-Console evidence.
