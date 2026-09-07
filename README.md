# لوحة Linux/Termux (Linux Termux Panel)

تطبيق أندرويد بواجهة Jetpack Compose لإدارة وتشغيل أوامر Linux/Termux من واجهة رسومية:
حفظ الأوامر، إدارة الخدمات (تشغيل/إيقاف/حالة/إعادة تشغيل)، وسجل كامل للتنفيذات.

## المتطلبات

| المتطلب | القيمة |
|---|---|
| JDK | 17 |
| Android Gradle Plugin | 8.3.0 |
| Gradle | 8.5 |
| Kotlin | 1.9.22 |
| compileSdk / targetSdk | 34 |
| minSdk | 21 |

## البناء

```bash
chmod +x ./gradlew
./gradlew assembleDebug        # ينتج app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # اختبارات الوحدة
```

عند البناء محليًا أنشئ ملف `local.properties` (غير مرفوع إلى Git) يحتوي على:

```properties
sdk.dir=/المسار/إلى/Android/Sdk
```

أو اضبط متغيّر البيئة `ANDROID_HOME`.

يوجد أيضًا سير عمل GitHub Actions (`.github/workflows/android.yml`) يبني نسخة APK للتصحيح
ويرفعها كملف artifact عند كل دفع.

## تشغيل الأوامر عبر Termux — خطوات إلزامية

التطبيق يرسل الأوامر إلى خدمة `RunCommandService` داخل Termux، ولكي تعمل يجب:

1. تثبيت **Termux** من F-Droid أو GitHub (نسخة Google Play قديمة ولا تدعم هذا).
2. تفعيل التطبيقات الخارجية داخل Termux:

   ```bash
   mkdir -p ~/.termux
   echo "allow-external-apps=true" >> ~/.termux/termux.properties
   termux-reload-settings
   ```

3. التطبيق يطلب الصلاحية `com.termux.permission.RUN_COMMAND` (معرّفة في `AndroidManifest.xml`).
4. لأوامر Ubuntu ثبّت proot-distro داخل Termux:

   ```bash
   pkg install proot-distro
   proot-distro install ubuntu
   ```

> ملاحظة: عند غياب Termux يتحوّل التطبيق تلقائيًا إلى تنفيذ الأوامر داخل شِل أندرويد
> (`sh -c`) وهو محدود بصلاحيات صندوق التطبيق.

## بنية المشروع

```
app/src/main/java/com/example/linuxtermuxpanel/
├── data/
│   ├── local/          # Room: قاعدة البيانات وDAOs والمحوّلات
│   ├── model/          # الكيانات + إعدادات التطبيق
│   ├── preferences/    # SettingsRepository (DataStore)
│   └── repository/     # مستودعات الأوامر/الخدمات/السجل
├── di/                 # وحدات Hilt
├── execution/          # طبقة تنفيذ الأوامر (Termux + شِل احتياطي + لفّ Ubuntu)
└── ui/
    ├── commands/ services/ history/ settings/ dashboard/
    ├── components/     # عناصر واجهة مشتركة
    ├── navigation/     # المسارات ودوال التنقّل
    ├── theme/
    └── viewmodel/
```

## وضع التنفيذ

- **Termux (خلفية)**: يُنفَّذ الأمر عبر `bash -lc` وتُلتقط المخرجات (stdout/stderr/exitCode)
  عبر `PendingIntent` وتُحفظ في السجل.
- **Termux (تفاعلي)**: عند تفعيل «يحتاج طرفية تفاعلية» يُفتح الأمر في جلسة Termux،
  وفي هذه الحالة لا يمكن التقاط المخرجات.
- **Ubuntu**: يُلفّ الأمر تلقائيًا بالشكل
  `proot-distro login ubuntu -- bash -lc '<الأمر>'` (يمكن تعديل أمر الدخول من الإعدادات).
