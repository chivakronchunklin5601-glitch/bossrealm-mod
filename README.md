# Boss Realm — Minecraft 1.20.1 / Forge 47.4.28

มอดบอส 3 ตัว (ง่าย / ปานกลาง / ยาก) มีอนิเมชั่น keyframe ตอนโจมตี, สกิลหลากหลาย, เสียง, บอสบาร์, เฟส 2 และ loot
ใช้ระบบอนิเมชั่นของ Minecraft เอง (`AnimationDefinition`) **ไม่ต้องพึ่ง GeckoLib**

## วิธี build

1. ต้องใช้ **JDK 17**
2. โปรเจกต์นี้ยังไม่มี Gradle wrapper (`gradlew`, `gradle/wrapper/`) ให้คัดลอกสองอย่างนี้มาจาก Forge MDK 1.20.1 ทางการ
   (https://files.minecraftforge.net → 1.20.1 → Mdk) มาวางที่โฟลเดอร์นี้ หรือเปิดโฟลเดอร์นี้ด้วย IntelliJ IDEA แล้วให้มัน import Gradle เอง
3. คำสั่ง
   - ทดลองเล่น: `./gradlew runClient`
   - สร้างไฟล์ mod: `./gradlew build` → ได้ไฟล์ใน `build/libs/bossrealm-1.0.0.jar`

## วิธีเรียกบอส

- ไข่เสกในแท็บ Spawn Eggs ของ Creative
- หรือคำสั่ง `/summon bossrealm:bone_warden` · `/summon bossrealm:infernal_reaver` · `/summon bossrealm:void_sovereign`

## ตัวเลขของบอส

| บอส | ระดับ | เลือด | เกราะ | Toughness | ดาเมจพื้นฐาน |
|---|---|---|---|---|---|
| Bone Warden (ผู้พิทักษ์กระดูก) | ง่าย | 200 | 4 | 0 | 9 |
| Infernal Reaver (จอมมารเพลิงนรก) | ปานกลาง | 350 | 8 | 0 | 12 |
| Void Sovereign (จักรพรรดิแห่งความว่างเปล่า) | ยาก | 500 | 12 | 2 | 14 |

- ชุดเพชรที่ไม่เอนจันท์ = เกราะ 20 / toughness 8 → ทุกตัวต่ำกว่านั้น
- ในโค้ด `BossBase.bossAttributes(...)` มี **เพดานบังคับ** 500 เลือด / เกราะ 20 / toughness 8 ใส่เลขเกินก็ถูกตัดลงเอง
- อยากปรับความยาก แก้ที่เมธอด `createAttributes()` ของแต่ละบอส (`entity/BoneWarden.java` ฯลฯ)
- ดาเมจจริงขึ้นกับความยากของเกมด้วย (Easy/Normal/Hard) และเกราะของผู้เล่น
- ดาเมจต่อหนึ่งครั้งที่ผู้เล่นตีบอสถูกจำกัดไม่เกิน 20% ของเลือดสูงสุด (กันเรื่องของโกง; `/kill` ยังใช้ได้)

## สกิลของบอส

**Bone Warden (ง่าย)** — จังหวะช้า เห็นท่าก่อนโจมตีชัด พักนาน
- Swipe: ฟาดแขนระยะใกล้ (เฟรมโดนที่ 0.5 วิ)
- Ground Slam: ทุบพื้น เกิดคลื่นกระแทกรัศมี 5.5 บล็อก ผลักและยกตัวผู้เล่น
- Bone Volley: ยิงลูกธนูกระดูก 5 ดอกเป็นรูปพัด
- Bone Call (เฟสที่ 2): เรียกโครงกระดูกมาช่วย 2 ตัว

**Infernal Reaver (ปานกลาง)** — เร็วขึ้น ผสมระยะใกล้/ไกล/วงกว้าง ภูมิคุ้มกันไฟ
- Blade Combo: ฟันดาบไฟ 2 ครั้งต่อเนื่อง พุ่งเข้าหา ติดไฟ
- Fireball Volley: ปล่อยลูกไฟ 3 ชุด ชุดละ 3 ลูก
- Flame Dash: ย่อตัวแล้วพุ่งชนเป็นเส้นตรง ทิ้งเปลวไฟ
- Fire Nova: ทุบพื้นเกิดวงไฟขยายออก (กระโดดข้ามได้)
- Inferno Rain (เฟสที่ 2): เสาไฟระเบิด 6 จุด มีวงเตือนก่อน 1 วินาทีครึ่ง

**Void Sovereign (ยาก)** — คูลดาวน์สั้น ลอยตัว วาร์ป ยิงลำแสง
- Void Slash: ฟัน 2 ที ทำให้ช้า
- Blink Strike: หายตัว วาร์ปไปด้านหลังเป้าหมาย แล้วฟันแรง
- Void Beam: ชาร์จแล้วยิงลำแสงกวาด ±20° ถูกบล็อกกั้นได้
- Gravity Well: ดูดทุกคนเข้าหา แล้วระเบิดเข้าด้านใน (วิ่งหนีให้พ้นรัศมี)
- Summon Wraiths: เรียก Vex 3 ตัว อยู่ได้ 20 วินาที
- Void Rain (เฟสที่ 2): วงเตือน 8 จุดแล้วระเบิดพร้อมกัน

เฟส 2 เริ่มตอนเลือดเหลือ 50%: ขยับเร็วขึ้น 20%, ดาเมจ +25%, คูลดาวน์สั้นลง 30%, และบอสใช้สกิลประจำเฟส 2 ทันที

## โครงสร้างโค้ด

```
src/main/java/com/bossrealm/
  BossRealm.java            ตัวมอดหลัก
  entity/BossBase.java      ระบบสกิล, บอสบาร์, เฟส 2, ฟังก์ชันช่วย (hitCone, hitRadius, ฯลฯ)
  entity/BoneWarden.java    บอสง่าย
  entity/InfernalReaver.java บอสกลาง
  entity/VoidSovereign.java บอสยาก
  client/BossAnimations.java คีย์เฟรมอนิเมชั่นทุกท่า
  client/BossModelLayers.java โมเดล (สร้างพร้อมเท็กซ์เจอร์)
  client/BossModel / BossRenderer / BossGlowLayer
src/main/resources/assets/bossrealm/textures/entity/  เท็กซ์เจอร์ + *_glow.png (ส่วนที่เรืองแสง)
src/main/resources/data/bossrealm/loot_tables/entities/ ของที่ดรอป
```

**อนิเมชั่นกับสกิลผูกกัน:** ความยาวคลิปอนิเมชั่น = ระยะเวลาสกิล (วินาที × 20 tick) และเฟรมที่ "ฟาด" ตรงกับ tick ที่โค้ดทำดาเมจ
ถ้าจะแก้จังหวะ ต้องแก้ทั้งเลข tick ในคลาสบอส และเวลาของคีย์เฟรมใน `BossAnimations.java` ให้ตรงกัน

## ของที่ดรอป

- Bone Warden: กระดูก, เหล็ก, เพชร, หัวโครงกระดูก (25%)
- Infernal Reaver: Blaze Rod, ทอง, Netherite Scrap, Magma Block (30%)
- Void Sovereign: Ender Pearl, เพชร, Nether Star, Netherite Ingot (50%), Elytra (25%)

แก้ได้ที่ `data/bossrealm/loot_tables/entities/*.json`

## หมายเหตุ

โค้ดนี้เขียนโดยไม่ได้คอมไพล์หรือเปิดเกมทดสอบ (สภาพแวดล้อมที่สร้างไม่มี Forge และต่อเน็ตไม่ได้)
ตรวจแล้วว่าไวยากรณ์ Java ถูกต้องและไฟล์ JSON ใช้ได้ แต่ถ้า build แล้วเจอ error หรือท่าทางบอสดูแปลก (เช่น ทิศแขนกลับด้าน)
ส่งข้อความ error หรือรายละเอียดมาได้ แก้ได้ทันที
