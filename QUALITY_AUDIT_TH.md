# Zipspeed v71 — Quality / Structure Audit

วันที่อัปเดต: 2026-09-23

## สถานะโครงสร้างหลัง cleanup

แหล่งที่ใช้งานจริงถูกลดให้เหลือชัดเจน:

- Web / Google AI Studio: `public/index.html`
- Android native: `app/src/main/`
- Android bundled HTML: `app/src/main/assets/index.html`

ชุด legacy และ generated files ที่ไม่ใช่ runtime ถูกลบออกแล้ว เช่น root `index.html`, `Zipspeed_Space_v3`, ZIP เก่า, patch/script ชั่วคราว, preview เก่า, `.gradle`, `.idea` และไฟล์ว่าง/เครื่องมือแก้ครั้งเดียว

## สิ่งที่ CI ตรวจ

- Web build
- UI audit สำหรับ canonical entry points
- Android unit tests
- Android `lintDebug`
- Android `assembleDebug`
- Debug APK artifact
- Play/source gate
- Version/package/API invariants
- Source hygiene: legacy paths สำคัญต้องไม่กลับเข้ามา

## กฎ source of truth

- ห้ามสร้าง entry point เว็บใหม่ที่ root เพื่อแก้เฉพาะหน้า
- ห้ามนำ `Zipspeed_Space_v3` หรือ ZIP โปรเจกต์เก่ากลับมา
- AI Studio ต้อง sync/import จาก `main` ของ `dachopol/Zipspeed`
- หาก AI Studio แสดงไฟล์อย่าง `app/applet/`, `src/app.mjs`, `audit.mjs` หรือ `runtime-check.mjs` ให้ถือว่าเป็น local project เก่าจนกว่าจะยืนยันว่ามีไฟล์นั้นใน GitHub main จริง

## คุณภาพที่ยังต้องตรวจบนอุปกรณ์จริง

- GO/STOP และการแตะทุก state
- การเคลื่อนไหวของเข็มบนเครื่องจริง
- offline / timeout / network change
- จอเล็ก, landscape, safe area และ keyboard
- Thai font/wrapping
- TalkBack/focus order/contrast
- history/share/export
- GPS optional flow
- พฤติกรรมบนเครือข่ายจริงหลายประเภท

## สถานะ release

v71 ผ่าน source/build checks ใน CI แต่ signed AAB และสถานะ Play Console สดยังเป็น TO VERIFY จนกว่าจะมี upload key จริงและหลักฐานจาก Console
