# Zipspeed v71 — Play Console TEST BUILD Gate

Status: **TEST BUILD**  
Android version: **71.0.0** (`versionCode 71`)  
Web project version: **71.0.0**  
Application ID: `com.aistudio.zipspeed.zskt`

This gate separates repository/build checks from live Google Play Console checks. A green CI run is not, by itself, proof that the app is approved for Production.

## Automated PASS gate

`npm run play:check` verifies source-level invariants:

- application ID remains `com.aistudio.zipspeed.zskt`
- compile SDK 36
- target SDK 36
- Android `versionCode 71`
- Android `versionName 71.0.0`
- web project version 71.0.0
- required network permissions remain present
- CI contains Android unit tests, `lintDebug`, `assembleDebug`, Play gate and evidence artifact
- signed `bundleRelease` is explicitly gated behind release-signing configuration

CI artifacts:

- `zipspeed-web-dist`
- `zipspeed-play-console-evidence`
- `zipspeed-v71-debug-apk`
- `zipspeed-v71-signed-aab` only when signed-release configuration is explicitly enabled and valid

## Google Play requirements locked into this TEST BUILD checklist

### Target API

For Android mobile apps, new apps and app updates submitted from 31 August 2026 must target Android 16 / API 36 or higher.

Official source:
https://support.google.com/googleplay/android-developer/answer/11926878

### New personal developer account testing

For personal developer accounts created after 13 November 2023 that are subject to the production-access testing rule, a closed test must have at least 12 testers continuously opted in for at least 14 days before applying for Production access.

Official source:
https://support.google.com/googleplay/android-developer/answer/14151465

### Data Safety

Apps active on closed, open or production tracks must complete the Data Safety form. Apps exclusively active on internal testing are exempt from inclusion in the Data Safety section while they remain internal-only.

Official source:
https://support.google.com/googleplay/android-developer/answer/10787469

### Package registration / developer verification

Play developers should check Play Console and register any remaining package names that are not already registered. Google states a 30 September 2026 deadline for remaining apps that developers want to continue distributing.

Official source:
https://developer.android.com/developer-verification/guides/google-play-console

## TO VERIFY / UNVERIFIED before calling the app “ready to publish”

- Play App Signing state in the live Play Console
- correct upload key / keystore ownership
- signed release AAB generated with the real upload key
- signed AAB accepted by the correct Play Console app
- Privacy Policy URL and content match the exact release behavior
- Data Safety answers match the exact release artifact and every enabled SDK
- Ads declaration
- App access declaration
- Target audience and content
- IARC Content Rating
- closed-test eligibility/status for this developer account
- package-name registration/developer-verification status
- real-device checks for GO/STOP, gauge motion, download/upload, offline/error recovery, history/share/export and responsive layouts

## Accuracy rule

Do not label a live-console item PASS from repository source alone. Items that require Play Console account state, signing secrets, policy forms, tester history, device testing or upload acceptance remain **TO VERIFY / UNVERIFIED** until evidence exists.
