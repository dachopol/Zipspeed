# Zipspeed — Google Play listing draft based on current code

**App name:** Zipspeed  
**Credit:** by AnakinYoo  
**Category direction:** Tools

> This document describes only features currently supported by the Android project. Do not add claims that are not backed by the release build.

## Short description

ทดสอบ Download/Upload, HTTP Latency/Jitter, IP และบันทึกผลเครือข่าย

## Full description draft

**Zipspeed by AnakinYoo** เป็นเครื่องมือทดสอบและตรวจสอบเครือข่ายสำหรับ Android ที่เน้นแสดงค่าที่วัดได้จริง หาก endpoint หรือเครือข่ายไม่ตอบ แอปจะแสดงสถานะผิดพลาดหรือไม่มีข้อมูลแทนการสร้างค่าจำลอง

ฟังก์ชันหลักประกอบด้วยการวัด Download และ Upload ผ่าน HTTP, วัด HTTP latency และ jitter, แสดง Public/Local IP เมื่อดึงข้อมูลได้, ทดสอบความเหมาะสมสำหรับการสตรีมจาก throughput ที่วัดได้, ตรวจเวลาเปิดเว็บไซต์/endpoint, เก็บประวัติผลในเครื่อง และแชร์หรือส่งออกประวัติเป็น CSV/JSON

แอปรองรับภาษาไทยและอังกฤษ, Dark/Light mode, Reduced Motion และหน้าจอหลายขนาด การใช้ GPS เป็นตัวเลือกและไม่จำเป็นต่อการเริ่มทดสอบความเร็ว

## Features verified in the current project

### Speed test
- Download throughput from transferred HTTP payload bytes.
- Upload throughput from transferred HTTP payload bytes.
- HTTP latency and jitter.
- Live gauge/needle and GO/cancel/retry flow.
- Mbps / MB/s display units.
- Cloudflare Anycast endpoint. Actual PoP/location is displayed only when verifiable metadata is returned.

### Network information
- Public IP when available.
- Local IPv4 when available.
- ISP/ASN metadata only when returned by the endpoint.
- Optional GPS mode; speed testing itself does not require location permission.

### Video suitability
- Measures HTTP payload throughput across resolution thresholds from 480p through 4K.
- The result is a network-throughput suitability estimate, not actual video playback or a licensed streaming benchmark.

### Website checks
- HTTP checks against configured public endpoints.
- Displays measured response timing and HTTP result status.
- Does not claim separate DNS-resolution or TLS-handshake timing unless those measurements are implemented in the release.

### Status links
- Opens external outage/status websites for supported services.
- Zipspeed does not invent live outage counts or operational/outage labels.

### History and sharing
- Completed measured results can be stored locally using Room.
- Individual results can be shared.
- History can be exported as CSV/JSON.
- Unknown metrics remain unknown instead of being replaced with fabricated zero/default values.

### UI
- Thai / English.
- Dark / Light.
- Reduced Motion / Battery Saver controls.
- Phone bottom navigation and side navigation on wider screens.

## Features not ready to advertise yet

Do **not** claim these as production features until the code and account configuration exist and pass testing:

- ICMP packet-loss measurement.
- Manual city-specific worldwide speed-test servers.
- Live outage report counts.
- Regional ISP rankings/benchmarks.
- Indoor Wi-Fi position/heatmap derived from real coordinates.
- Real AdMob impressions/interstitial/rewarded ads.
- Paid Ad-Free entitlement through Google Play Billing.
- Play Integrity verification.
- Guaranteed equivalence to Ookla, Cloudflare Speed Test or any third-party benchmark.

## Privacy/Data Safety implementation notes

- Speed/video tests contact configured Cloudflare HTTP endpoints.
- Public-IP fallback may contact api.ipify.org.
- External outage links open third-party websites.
- GPS is optional and requested only when the user selects GPS mode.
- Test history is stored locally in the app database.
- Privacy Policy and Play Console Data Safety answers must be checked again whenever ads, analytics, billing, crash reporting or other SDKs are enabled.

## Screenshot copy based on implemented features

| Screen | Headline | Supporting text |
|---|---|---|
| 1 | วัด Download / Upload แบบเรียลไทม์ | เข็มและตัวเลขขยับตามค่าที่วัดได้ |
| 2 | ดู HTTP Latency และ Jitter | ถ้าวัดไม่ได้ แอปจะแจ้งข้อผิดพลาด |
| 3 | เช็ก IP และเส้นทาง Anycast | แสดง PoP เฉพาะเมื่อมีข้อมูลที่ตรวจสอบได้ |
| 4 | ทดสอบความเหมาะสมสำหรับวิดีโอ | ประเมินจาก throughput ที่วัดได้จริง |
| 5 | เก็บและแชร์ประวัติผล | ส่งออก CSV/JSON จากผลที่วัดสำเร็จ |

## ASO terms that match the current build

`internet speed test`, `network test`, `download speed`, `upload speed`, `latency`, `jitter`, `IP address`, `network diagnostics`, `วัดความเร็วเน็ต`, `ทดสอบอินเทอร์เน็ต`, `เช็ก IP`
