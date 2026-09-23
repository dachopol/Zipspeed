# Zipspeed by AnakinYoo — v71

เวอร์ชันทดสอบปัจจุบัน: **71.0.0** (`versionCode 71`)  
Package ID: `com.aistudio.zipspeed.zskt`

## โครงสร้างที่ใช้งานจริง

- Android: `app/src/main/`
- HTML ที่ bundle ใน Android: `app/src/main/assets/index.html`
- Web / Google AI Studio: `public/index.html`
- ผล build เว็บ: `dist/` (สร้างอัตโนมัติ ไม่เก็บใน Git)
- Play/source gate: `play-console-check.mjs`

ไฟล์เก่าที่ซ้ำ เช่น root `index.html`, `Zipspeed_Space_v3`, ZIP เก่า, patch/script แก้ชั่วคราว, preview เก่า, `.gradle` และ `.idea` ถูกลบออกแล้ว และ CI จะตรวจไม่ให้กลับเข้ามาอีก

## ใช้กับ Google AI Studio

ให้ Import/Sync จาก **branch `main` ของ `dachopol/Zipspeed` เท่านั้น**

ถ้า AI Studio ยังเห็นไฟล์เก่า เช่น `app/applet/`, `src/app.mjs`, `audit.mjs` หรือ `runtime-check.mjs` แสดงว่ายังเป็น local project เก่า ไม่ใช่โครงสร้าง GitHub ปัจจุบัน ให้ดึงจาก `main` ใหม่

Web build ปัจจุบันไม่ต้องใช้ Gemini API key และ metadata ไม่ร้องขอ geolocation หรือ Gemini server capability

## คำสั่งเว็บ

```bash
npm start
npm run ui:audit
npm run build
npm run play:check
```

## คำสั่ง Android CI

```bash
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

CI v71 ตรวจ unit tests, lint, debug APK, web build, UI audit และ Play source gate อัตโนมัติ

Signed AAB ยังต้องใช้ upload key จริงและเปิด `ENABLE_SIGNED_RELEASE=true` ก่อน จึงยังไม่ถือว่า Play Console production พร้อมจาก source gate เพียงอย่างเดียว

## หลักข้อมูลจริง

ห้ามสุ่มหรือเติมค่าทดสอบเน็ต, ISP, เมืองเซิร์ฟเวอร์, outage, โฆษณา, การซื้อ หรือสถานะ security ที่ไม่มีหลักฐานจริง หากไม่มีข้อมูลให้แสดงไม่ทราบ/ไม่พร้อมใช้งาน

ดูเพิ่ม:
- `PLAY_CONSOLE_TEST_GATE.md`
- `PLAY_STORE_RELEASE_AUDIT.md`
- `DATA_SAFETY_WORKSHEET.md`
