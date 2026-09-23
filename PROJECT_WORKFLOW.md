# Zipspeed Project Workflow — Permanent Rules

Project: **Zipspeed by AnakinYoo**  
Repository Source of Truth: **dachopol/Zipspeed / main**  
Current release line: **71.0.0 / versionCode 71**  
Android package: **com.aistudio.zipspeed.zskt**

This workflow is mandatory for every Zipspeed change. Do not skip forward to UI polish or release work when an earlier gate is unresolved.

## Required order

### 1. Source of Truth
- GitHub `main` is the canonical remote source.
- Android source: `app/src/main/`.
- Web / AI Studio source: `public/index.html`.
- Android bundled web source: `app/src/main/assets/index.html`.
- Generated/local/legacy copies must not be treated as authoritative.

### 2. GitHub Remote Check
- Verify repository, branch and current HEAD before editing.
- Compare AI Studio/local files with GitHub before accepting local state.
- If AI Studio shows files absent from GitHub `main`, treat them as stale until verified.
- Do not claim a commit/build exists unless it can be retrieved.

### 3. Anti-Random / Real Data
- Never Fake / Random / Hardcode values that are presented as real measurements or live status.
- This includes Download, Upload, Latency, Jitter, Packet Loss, IP, ISP, ASN, server city/PoP, outage state, ads, purchases, security state and entitlements.
- Missing/unverified data must remain `ไม่ทราบ`, `Unknown`, `--` or `Error` as appropriate.
- No pre-filled ratings, stars, quality grades or usage suitability before the measurements required for that result exist.
- Random bytes may be generated only as transport payload where randomness itself is not presented as a measured result.

### 4. Working App Validation
- Validate the existing working path before redesigning it.
- Web: project policy → UI audit → build.
- Android: unit tests → lint → debug build.
- Validate GO/STOP/Retest, gauge motion, real data flow, cancel, offline/error recovery and history/share behavior.
- A green compile is not proof of real-device behavior.

### 5. Root-Cause Error Fix
- Read the actual error/log first.
- Fix the smallest verified root cause.
- Do not randomly edit source to make Preview appear.
- If compile/build passes but Preview is stuck, inspect emulator/runtime/dev-server/assets/routes/service worker/WebView first.

### 6. Version Consistency
- Android `versionCode` and `versionName`, web `package.json`, release docs and generated artifacts must agree.
- Package ID must stay `com.aistudio.zipspeed.zskt`.
- Never reduce version during repair.

### 7. UI Card-Only
- Primary screen flow: **Header → Status Card (when needed) → Vertical Card List**.
- Card rows should prefer **name/label on the left → current state/value on the right**.
- Ready/Passed/Available states do not need technical explanatory text.
- Error state may show concise cause and corrective point.
- Remove only unnecessary explanation/technical metadata; do not remove core controls, navigation, routes or capabilities to solve layout issues.

### 8. Responsive / Auto Layout
- Fix UI with responsive/adaptive layout, not feature removal.
- Support small phones, tablets and landscape.
- Respect safe areas.
- Important touch targets should be about 44×44 dp/px CSS-equivalent or larger.
- Do not shrink the whole UI font to hide overflow.
- Text must wrap/flow without cutting Thai or English content.

### 9. Visual System
- Keep one consistent spacing, card, typography, status and motion system.
- Visual polish must never change measurement truth or hide errors.

### 10. Language / Region
- Thai and English must both be complete.
- EN mode must actually change the UI, not only selected labels.
- Do not truncate translations to solve layout problems.
- Region/server/location text must be based only on verified data.

### 11. Security / Privacy
- Do not display simulated security verification as real.
- Do not expose secrets, signing keys or private tokens.
- Privacy/Data Safety must match actual runtime behavior and enabled SDKs.
- Ads/Billing/Ad-Free must not appear active until real integrations and entitlement verification exist.

### 12. Build System
Required CI gates:
- Project policy / source-of-truth gate.
- Canonical UI audit.
- Web build.
- Android unit tests.
- Android lint.
- Android debug APK.
- Play/source gate.
- Signed release AAB only when real signing configuration is explicitly enabled.

### 13. ZIP Delivery
- Delivery ZIP must be generated from the verified current source.
- Exclude `.git`, `.gradle`, `.idea`, `build`, `dist`, secrets, signing keys and stale legacy copies.
- A source ZIP is a delivery artifact, never a new source of truth.

### 14. Play Store / Release
- Do not call the app production-ready from repository build success alone.
- Verify real signing, signed AAB, Play Console upload acceptance, Privacy Policy, Data Safety, declarations and account/test requirements separately.
- Store claims must describe only implemented behavior.

### 15. Final QA Gate
Run after functional/build gates:
- data truth / empty states
- error states
- responsive layout / safe area
- Thai/English
- accessibility / touch targets
- visual consistency
- real-device behavior
- release declarations

### 16. Delivery Report
Every delivery report must state:
- verified source HEAD / branch
- version/package
- checks that actually passed
- artifacts actually generated
- changes made
- removed/disabled behavior
- remaining `TO VERIFY`
- no unsupported PASS claims

## Stop conditions

Stop and mark `TO VERIFY` rather than guessing when evidence is missing. A later workflow stage must not overwrite an unresolved earlier-stage failure.
