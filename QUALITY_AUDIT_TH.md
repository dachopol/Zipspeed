# Zipspeed — ตรวจสอบคุณภาพ UI/UX และแผนทำงานต่อ

วันที่ตรวจ: 2026-09-20

เอกสารนี้เป็นผลตรวจจากโครงสร้างและไฟล์ที่อยู่ใน `dachopol/Zipspeed` หลังการรีดีไซน์ล่าสุด โดยแยกประเด็นที่แก้ได้ทันทีออกจากสิ่งที่ต้องทดสอบบนอุปกรณ์จริง

## สรุปสถานะ

- CRITIQUE: 6/10 — โครงสร้างหลักชัดขึ้นจากการมีปุ่ม GO จุดเดียว แต่ยังมี entry point หลายชุดและภาพรวมของเว็บ/Android ไม่เป็นแหล่งจริงเดียวกัน
- TYPESET: 6/10 — มีการใช้ตัวเลขแบบ tabular และรองรับภาษาไทย แต่ต้องตรวจ encoding และ line wrapping ให้ครบทุกเส้นทางภาษา
- LAYOUT: 5/10 — mobile-first และมี safe-area บางส่วนแล้ว แต่ต้องตรวจซ้ำในจอเล็ก/จอสูง/keyboard และไม่ควรมี UI เวอร์ชันซ้ำที่แก้ไม่พร้อมกัน
- POLISH: 5/10 — มี reduced-motion, loading/cancel และ history แล้ว แต่ยังต้องยืนยันทุก state ด้วย browser และ Android build จริง

## สิ่งที่ตรวจพบ

### 1. แหล่งโค้ดซ้ำและสถาปัตยกรรม

มีหน้าเว็บที่ทำหน้าที่คล้ายกันหลายตำแหน่ง:

- `index.html`
- `public/index.html`
- `Zipspeed_Space_v3/index.html`
- `app/src/main/assets/index.html`

`index.html` และ `public/index.html` เป็นเวอร์ชันเดียวกันโดยพฤตินัย แต่ `Zipspeed_Space_v3/index.html` เป็น UI/logic อีกชุดหนึ่ง ส่วน Android ยัง bundle หน้าเว็บไว้ใน assets ด้วย จึงเสี่ยงแก้ไฟล์หนึ่งแล้วผู้ใช้เห็นอีกไฟล์หนึ่ง

ข้อเสนอที่ต้องทำต่อ: กำหนด source of truth หนึ่งชุด แล้วให้ build/copy ไปยังปลายทางที่จำเป็น พร้อมเพิ่ม check ใน CI ว่าไฟล์สำคัญไม่ drift

### 2. เอกสารไม่ตรงกับสถานะปัจจุบัน

`README.md` ยังอธิบายการเปิด Android Studio และ Gemini API key ขณะที่ `README_TH.md` อธิบาย static web app/Node.js แล้ว ผู้ใช้จึงอาจทำตามคนละสถาปัตยกรรม

ข้อเสนอที่ต้องทำต่อ: ปรับ README หลักให้ระบุชัดว่า repository นี้มี Web และ Android legacy/bundle อย่างไร, คำสั่งรันที่ถูกต้องคืออะไร, และไฟล์ใดเป็นตัวจริง

### 3. Accessibility

สิ่งที่ทำไว้แล้ว:

- ปุ่มหลักเป็น `<button>` จริง
- มี `aria-live` สำหรับสถานะ
- มี `focus-visible`
- รองรับ `prefers-reduced-motion`
- canvas ฉากหลังถูกทำเป็น `aria-hidden`

สิ่งที่ต้องตรวจ/แก้ต่อ:

- ปุ่มนำทางทุกปุ่มต้องมี accessible name ที่ชัดเจน แม้จะแสดงเฉพาะไอคอน
- canvas gauge ต้องมีข้อความผลลัพธ์ที่ screen reader อ่านได้ ไม่พึ่ง `aria-label` ของ canvas เพียงอย่างเดียว
- สีสถานะ online/offline และ download/upload ต้องไม่สื่อความหมายด้วยสีอย่างเดียว
- ตรวจ contrast ของข้อความรองบนทั้ง dark และ light theme
- ตรวจ focus order เมื่อเปิด modal และคืน focus ไปยังปุ่มเปิด modal
- `target="_blank"` ต้องมี `rel="noopener noreferrer"`

### 4. Typography และภาษาไทย

- ตัวเลขควรใช้ `font-variant-numeric: tabular-nums` ต่อเนื่องทุกจุดที่เปลี่ยนระหว่างการทดสอบ
- ควรใช้ `line-height` อย่างน้อยประมาณ 1.5 สำหรับข้อความไทย และไม่กำหนดความสูงคงที่ให้พื้นที่ที่มีข้อความหลายภาษา
- ต้องตรวจข้อความที่มีอักขระเพี้ยน (`�`) ในไฟล์/ผลลัพธ์ที่ถูก bundle และตั้ง encoding เป็น UTF-8 ตลอด pipeline
- หลีกเลี่ยงการตัดคำไทยด้วย CSS ที่บังคับ `word-break` แบบเดียวกับภาษาอังกฤษ
- ข้อความปุ่ม GO/STOP ควรมี accessible label ที่เปลี่ยนตาม state และภาษา

### 5. Layout และ safe area

- ใช้ `100dvh` และ `env(safe-area-inset-bottom)` แล้ว แต่ต้องทดสอบร่วมกับ bottom navigation, landscape และ browser toolbar ที่ยุบ/ขยาย
- ตรวจ breakpoint ต่ำกว่า 360px เพราะ gauge, metrics และปุ่มแชร์อาจเบียดกัน
- เพิ่ม spacing token กลางแทนค่าที่กระจายอยู่ใน CSS เพื่อให้ rhythm สม่ำเสมอ
- ไม่ควรใช้ inline style กับโครงสร้างหลัก เช่น history clear action หากต้องรองรับ theme/state

### 6. State และความถูกต้องของการวัด

- จุดแข็งคือมีการยกเลิก test และกันการกดซ้ำแล้ว
- ต้องทดสอบ state: ready, warm-up, latency, download, upload, completed, cancelled, offline, timeout และ storage failure
- ต้องตรวจว่าการยกเลิกไม่เขียน history ผลลัพธ์บางส่วน
- ต้องตรวจว่าผล upload เป็นข้อมูลที่ server ยืนยันการรับจริง ไม่ใช่เพียงเวลาที่ client ส่ง request
- ต้องจำกัดข้อมูล history และจัดการกรณี localStorage เต็ม/ถูกปิด

## ลำดับการทำงานต่อ

1. เลือก source of truth ของเว็บและลบ/ทำเครื่องหมายไฟล์ UI ที่ซ้ำ
2. แก้ README ให้ตรงกับสถาปัตยกรรมจริง
3. เพิ่ม automated checks สำหรับ encoding, duplicate entry point, accessible names และ reduced motion
4. รัน browser regression ที่มี fixture สำหรับทุก test state
5. คืน Gradle wrapper/ตรวจ dependency แล้ว compile Android
6. ทดสอบบน Android device จริง: safe area, Thai font, cancel, rotation, TalkBack และ network change
7. ค่อยทำ visual polish รอบสุดท้ายหลัง functional states ผ่านทั้งหมด

## ขอบเขตการตรวจ

รีวิวนี้ครอบคลุม repository `dachopol/Zipspeed` ซึ่งเป็น repository ที่มี UI speed-test และไฟล์เป้าหมายจากคำขอก่อนหน้า ส่วน `dachopol/-QRCODE-` เป็น Android/Kotlin คนละแอป (`ง่ายสะดวก`) และไม่มีหลักฐานว่าเป็น target ของการรีดีไซน์ Zipspeed จึงยังไม่แก้ไข repository นั้น
