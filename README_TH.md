# Zipspeed สำหรับทำต่อใน AI Studio (Web)

ชุดใหม่ล้วน: หน้าอวกาศอยู่ที่ public/index.html ไม่มี Kotlin/Gradle
รัน npm run dev หรือ npm start; build ด้วย npm run build; ผลลัพธ์อยู่ dist
ใช้ Node.js ไม่ต้องติดตั้งแพ็กเกจเสริม ไม่ต้องใช้ Gemini API key เพื่อเทสความเร็ว

นำไฟล์ทั้งหมดใน ZIP ไปไว้ที่ root ของ GitHub repository แล้วใช้ AI Studio Build > Add files (+) > Import from GitHub
หากเชื่อม repo เดิมอยู่แล้ว ใช้ Settings > GitHub เพื่อดึงการเปลี่ยนแปลง
การแทนที่โฟลเดอร์ต้องลบไฟล์แอปเก่าที่ไม่ใช้ด้วย ห้ามลบ .git หรือข้อมูลลับส่วนตัวโดยไม่ตรวจ
เอกสาร: https://ai.google.dev/gemini-api/docs/aistudio-build-mode
ไม่มีการยืนยันว่าอัปโหลด ZIP โดยตรงจะนำเข้าโปรเจกต์ได้

ทดสอบ build และ HTTP local แล้ว ยังไม่ได้ import/run ภายในบัญชี AI Studio จริง
ข้อจำกัดแอปและการทดสอบดู public/START_HERE_TH.txt
