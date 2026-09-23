# Zipspeed v71 — UI notes

แนวทาง UI ปัจจุบัน:

- ปุ่ม GO/STOP เป็น action หลักของ speed test
- เข็ม/ตัวเลขต้องอิงค่าที่วัดได้จริง
- ห้ามมีปุ่ม Start Test ซ้ำที่ทำงานทับกับ GO
- เมนูหลักต้อง responsive และกดได้ครบ
- Dark/Light, Thai/English และ reduced-motion ต้องคงอยู่
- Download/Upload/Latency/Jitter ที่ไม่มีผลจริงให้แสดง unknown/unavailable
- Server/PoP/ตำแหน่งต้องไม่แต่งข้อมูล
- Ads/Ad-Free ต้องไม่จำลอง entitlement หรือราคา production

CI v71 compile Android, lint และสร้าง debug APK ได้แล้ว แต่ยังต้องทดสอบ touch/needle/layout/accessibility บนอุปกรณ์ Android จริงก่อนสรุป production readiness.
