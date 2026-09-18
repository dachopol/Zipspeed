# Zipspeed UI Refresh

ปรับหน้าตาโดยคงฟังก์ชันหลักเดิมไว้:

- ใช้โทน Deep Navy + Zipspeed Mint + Violet เพื่อให้ดูทันสมัยและอ่านง่าย
- ปุ่ม GO กลางมาตรวัดเป็น action หลักเพียงจุดเดียว ลดปุ่มเริ่มทดสอบซ้ำ
- ระหว่างทดสอบจะแสดงปุ่มหยุดโดยเฉพาะ
- เมนูล่างเหลือ 5 เมนูหลัก: Speed, Video, Status, Map, History โดยไม่ต้องเลื่อนแนวนอน
- ย้าย Settings ไปไอคอนด้านบน และคง Ad-Free/VIP ไว้ด้านบน
- Download ใช้ Mint, Upload ใช้ Violet เพื่อแยกข้อมูลด้วยสีอย่างชัดเจน
- Share result และ Precision mode ใช้สีแบรนด์เดียวกัน
- คง Dark/Light theme, server, IP, history, share, ads/VIP, GPS และฟังก์ชันอื่นไว้

หมายเหตุ: ตรวจโครงสร้างไฟล์และ delimiter ของ Kotlin แล้ว แต่ไม่ได้รัน Gradle build ในสภาพแวดล้อมนี้เพราะโปรเจกต์ไม่มี Gradle wrapper (`gradlew`).
