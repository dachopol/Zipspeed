Zipspeed by AnakinYoo — Space v3

ลงทับ: สำรองไฟล์เดิม แล้วแตก ZIP และคัดลอกไฟล์ทั้งหมดในโฟลเดอร์ไปทับตำแหน่งเดิม
เปิด index.html ผ่าน HTTPS หรือ localhost เพื่อใช้ PWA; zipspeed_x5f_project.html เป็นไฟล์ชื่อเดิม

ปรับใหม่:
- ฉากดาวพิกัด 3D ฉายลง Canvas พุ่งเมื่อเริ่มทดสอบ พื้นกริด perspective
- มาตรวัดวงแหวนโลหะ เข็มสองด้านและแสง ปุ่ม GO/STOP และเมนูนูน
- สเกล log มีตัวเลขกำกับให้ค่าความเร็วต่ำเห็นเข็มเคลื่อนชัด
- ยกเลิกแอนิเมชันเข็มเก่าก่อนเริ่มชุดใหม่ ไม่รัน Speed/Video ชนกัน
- timeout เครือข่าย, จัดการ storage ผิดพลาด, cache เฉพาะ app shell ไม่ cache ผลเทส
- ปิดเอฟเฟกต์ได้ รองรับ reduced motion และพักฉากเมื่อซ่อนหน้า
- เก็บ Speed / Video / Status / Map / System Test / History / Share / Settings

ข้อจำกัดที่ต้องทราบ:
นี่คือเว็บ/PWA ใช้ Canvas และ CSS ทำภาพลักษณ์สามมิติ ไม่ใช่โปรเจกต์ Blender หรือ APK
ไม่มีไฟล์ .blend และไม่ใช่ทุกวัตถุเป็น mesh 3D
ทดสอบ syntax และตรรกะด้วย Node + Canvas และจำลอง DOM/endpoint สำเร็จ
การเปิดหน้าเบราว์เซอร์และตรวจภาพยังไม่ได้ทำ: ไม่มี browser binary และดาวน์โหลดไม่สำเร็จ
เซิร์ฟเวอร์ Cloudflare ตอบ HTTP 200 ในการตรวจ HEAD; ยังไม่ยืนยัน speed flow จริงในเบราว์เซอร์
ต้องทดสอบ Android Chrome, iPhone Safari, desktop และ CORS หลังนำขึ้นโฮสต์
ไม่รับรองทุกระบบหรือความแม่นยำเทียบแอป native
Video เป็นการประเมิน throughput ไม่ใช่เล่นสตรีมจริง; Map เป็นแผงพิกัด ไม่ใช่แผนที่ภูมิศาสตร์
PROBE LOSS คือ HTTP request ที่ล้มเหลว ไม่ใช่ ICMP packet loss
ภาษาอังกฤษแปลเฉพาะป้ายหลัก ข้อความสถานะและรายละเอียดบางส่วนเป็นภาษาไทย
ใช้ Cloudflare endpoint เดิมของโปรเจกต์ ต้องตรวจเงื่อนไขบริการก่อนเผยแพร่เชิงพาณิชย์
