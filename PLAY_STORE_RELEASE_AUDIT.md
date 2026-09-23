# Zipspeed — Play Store Release Audit

Audit date: 2026-09-23  
Project: Zipspeed by AnakinYoo  
Application ID: `com.aistudio.zipspeed.zskt`

This checklist is based on the current Android source in this repository. It does not claim Play approval before the signed release is tested in Play Console.

## Current release status

| Area | Status | Current source truth |
|---|---|---|
| Android target API | PASS | `targetSdk = 36` |
| Compile API | PASS | Android API 36 configuration |
| Versioning | PASS | `versionCode = 71`, `versionName = 71.0.0` |
| Debug build | CI CHECK | GitHub Actions runs unit tests + `:app:lintDebug` + `:app:assembleDebug` and uploads the debug APK |
| Play test gate | CI CHECK | `npm run play:check` verifies v71/API/package/CI source invariants and uploads evidence |
| Release signing | USER ACTION | Signed `bundleRelease` runs only when the real upload keystore secrets are configured and `ENABLE_SIGNED_RELEASE=true` |
| Core speed flow | IMPLEMENTED | HTTP latency/jitter + Download/Upload payload measurement |
| Fake network values | HARDENED | Unknown/unavailable is used instead of invented fallback values |
| Optional GPS | PASS | Normal speed test does not require GPS; GPS is user-selected |
| History | IMPLEMENTED | Room database, share, CSV/JSON export |
| Status endpoints | IMPLEMENTED | HTTP endpoint health is measured; no hard-coded latency/status |
| Outage data | LIMITED | External status links only; Zipspeed does not invent live outage counts |
| Video test | LIMITED | HTTP-throughput suitability estimate; not real video playback |
| Packet loss | NOT IMPLEMENTED | Must remain undisclosed/unknown until a supported measurement method is added |
| AdMob | NOT CONFIGURED | No real Mobile Ads SDK/App ID/ad units in the current release |
| Google Play Billing | NOT CONFIGURED | No paid entitlement should be sold or advertised as working yet |
| Play Integrity | NOT CONFIGURED | Basic local checks must not be described as Play Integrity verification |
| Privacy Policy | FINAL REVIEW REQUIRED | Must match the final SDKs/endpoints/permissions in the release |
| Data Safety | FINAL REVIEW REQUIRED | Must be completed from the exact production artifact and SDK behavior |

## Verified Google Play requirements relevant to this project

### Target API

Starting 2026-08-31, new Android mobile apps and app updates submitted to Google Play must target Android 16 / API 36 or higher. Zipspeed currently targets API 36.

Official source:
https://support.google.com/googleplay/android-developer/answer/11926878

### New personal developer account testing

If the Play Console account is a personal developer account created after 2023-11-13, Google currently requires a closed test with at least 12 testers opted in continuously for at least 14 days before applying for production access.

Official source:
https://support.google.com/googleplay/android-developer/answer/14151465

### Google Play Billing

The current Android Developers release notes list Google Play Billing Library 9.1.0 (released 2026-06-18). The deprecation table gives Billing Library 9 a new-app/update deadline of 2028-08-31, Billing 8 a deadline of 2027-08-31, and Billing 7 a deadline of 2026-08-31.

If paid Ad-Free is implemented now, use the current supported Play Billing implementation and verify purchase/restore/entitlement behavior before shipping. Do not grant entitlement from a local toggle.

Official sources:
https://developer.android.com/google/play/billing/release-notes
https://developer.android.com/google/play/billing/deprecation-faq

## Current permissions

The manifest currently requests:

- `INTERNET`
- `ACCESS_NETWORK_STATE`
- `VIBRATE`
- `POST_NOTIFICATIONS`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

Release review notes:

- Internet/network state are required by network testing.
- Vibration is used for gauge haptics.
- Notifications are relevant only if signal-alert notifications remain in the release.
- Location must remain optional and must be accurately disclosed. The core speed test must continue to work without it.
- Re-check whether both fine and coarse location are necessary before the final production AAB.

## Network and external services currently contacted

The current source can contact:

- `speed.cloudflare.com` for network payload measurement.
- Cloudflare trace / 1.1.1.1 endpoints for route/health checks.
- `api.ipify.org` as a public-IP fallback.
- Google / YouTube / Wikipedia public endpoints for user-triggered status checks.
- External outage/status websites only when the user taps the corresponding link.

These destinations must be reflected accurately in the Privacy Policy and Data Safety review where applicable.

## Monetization gate

Do not enable the following until real account configuration exists:

- AdMob banner/interstitial/rewarded ads.
- Paid Ad-Free.
- Subscription or one-time purchase.
- Reward-based temporary premium entitlement.

The current UI intentionally avoids simulated ad impressions, fake countdowns, fixed fake prices, PromptPay/TrueMoney placeholders, and local entitlement toggles.

Before monetization release:

1. Add real Mobile Ads configuration and consent flow.
2. Use test ad units during development.
3. Add current Play Billing.
4. Load product pricing from Google Play instead of hard-coding price/currency.
5. Verify purchase acknowledgement, restore/query purchases, pending/cancelled/error flows and entitlement persistence.
6. Update Privacy Policy and Data Safety for the actual SDK data behavior.
7. Re-run build/tests and Play pre-launch checks.

## Store listing truth rules

The listing may currently claim:

- HTTP Download/Upload measurement.
- HTTP latency and jitter.
- Public/local IP where available.
- Anycast routing with actual PoP shown only when verifiable.
- Throughput-based video suitability estimate.
- HTTP endpoint health checks.
- Local history, sharing and CSV/JSON export.
- Thai/English UI, dark/light and reduced-motion options.

The listing must not currently claim:

- ICMP packet loss measurement.
- A real manually selected city-specific worldwide server network.
- Live ISP outage counts produced by Zipspeed.
- Regional ISP rankings.
- A real indoor-position Wi-Fi heatmap.
- Active AdMob monetization.
- Working paid Ad-Free entitlement.
- Play Integrity verification.
- Certification/equivalent accuracy to a third-party speed-test provider.

## Release blockers before Production AAB

- Latest branch CI must pass after all current code changes.
- Real Android device tests still required for GO/STOP, gauge/needle, Download, Upload, offline/error recovery, GPS optional flow, history/share/export and wide-screen layouts.
- Release upload signing secrets must be supplied securely; never commit the keystore/passwords.
- Privacy Policy and Data Safety must be finalized against the exact production artifact.
- If monetization is desired in the first public release, AdMob and Play Billing must be implemented and tested first.
- Play Console listing/assets/content declarations must be completed.
- If the account is subject to the new-personal-account rule, complete the required closed test before production access.
