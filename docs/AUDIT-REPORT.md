# تقرير فحص المشروع وإصلاح الأخطاء

تاريخ الفحص: 2026-09-07 — الفرع: `arena/01a079af-linux-termux-panel`

تم فحص المشروع بالكامل (34 ملفًا: Gradle، المانيفست، الموارد، طبقة البيانات، طبقة التنفيذ،
الواجهة، وسير عمل CI). النتيجة: **المشروع كان يُبنى بنجاح لكنه لا يعمل عمليًا** — أي أن
الأخطاء لم تكن أخطاء ترجمة بل أخطاء منطقية ووظيفية تجعل التطبيق عديم الفائدة عند التشغيل.

---

## 1) أخطاء حرجة (التطبيق لا يؤدي وظيفته)

### 1.1 تنفيذ الأوامر عبر Termux كان معطّلًا كليًا
**الملف السابق:** `execution/FileBasedTermuxExecutor.kt`

| المشكلة | الشرح |
|---|---|
| `sendBroadcast` بدل `startService` | Termux يستقبل الأمر عبر خدمة `com.termux.app.RunCommandService`، ولا يوجد مستقبِل بث لهذا الإجراء، فالأمر كان يضيع بلا أثر. |
| كتابة السكربت في `externalCacheDir` | المسار `/sdcard/Android/data/<pkg>/cache` لا يستطيع Termux قراءته على أندرويد 10+ (Scoped Storage). |
| انتظار ملف `exitcode` | كان الانتظار ينتهي دائمًا بمهلة (timeout) لأن الملف لا يُنشأ أبدًا. |
| غياب `<queries>` في المانيفست | على أندرويد 11+ لا يستطيع التطبيق حتى رؤية حزمة Termux أو تشغيل خدمتها. |
| استخدام `FileProvider` مع بث | منح صلاحية URI عبر البث لا يصل إلى الخدمة. |

**الإصلاح:** ملف جديد `execution/TermuxRunCommandExecutor.kt` يستخدم
`startService/startForegroundService` مع `RunCommandService`، ويستقبل النتيجة
(`stdout`, `stderr`, `exitCode`, `errmsg`) عبر `PendingIntent` + `BroadcastReceiver` مؤقت
مع مهلة قابلة للضبط، ورسائل خطأ عربية واضحة (Termux غير مثبّت / رفض الصلاحية / انتهاء المهلة).
كما أُضيف قسم `<queries>` في المانيفست.

### 1.2 التعديل على الأوامر والخدمات لم يكن يُحفظ إطلاقًا
**الملفات:** `ui/commands/CommandsScreen.kt`, `ui/services/ServicesScreen.kt`

```kotlin
val commandToSave = editingCommand ?: Command(...).copy(...)   // ❌
```

بسبب أولوية المعامل `?:` فإن `.copy(...)` تُطبَّق على الكائن الجديد فقط. عند التعديل كانت
القيمة تساوي `editingCommand` الأصلي **بدون أي تعديل**، فيُحفظ السجل كما هو.

**الإصلاح:** استبدال منطق النموذج بالكامل بـ `CommandFormState` / `ServiceFormState`
(نمط حالة واحدة + `toCommand()` / `toService()`) مع تحديث `updatedAt`.

### 1.3 شاشة الإعدادات كانت وهمية
**الملفات:** `ui/viewmodel/SettingsViewModel.kt`, `ui/settings/SettingsScreen.kt`, `execution/CommandExecutor.kt`

- `saveSettings` كانت تحدّث متغيّرًا في الذاكرة فقط ويضيع عند إغلاق الشاشة
  (مع أن `datastore-preferences` كانت ضمن الاعتماديات دون استخدام).
- `CommandExecutor` كان ينشئ `Settings()` افتراضية داخليًا، لذلك اسم حزمة Termux والمهلة
  وأمر الدخول إلى Ubuntu **لم تكن تُستخدم أبدًا**.
- حقول الشاشة كانت تُهيّأ من `remember` قبل تحميل الإعدادات فلا تُحدَّث بعد وصولها.

**الإصلاح:** `data/preferences/SettingsRepository.kt` (DataStore) + `AppSettings`،
والقراءة منها داخل `CommandExecutor` قبل كل تنفيذ، وشاشة إعدادات مرتبطة بحالة الـ ViewModel
مع رسالة تأكيد (Snackbar) وزر استعادة الافتراضيات.

### 1.4 شاشة الخدمات بلا أي وظيفة تشغيل
كانت الخدمات تُضاف وتُعدَّل وتُحذف فقط — لا يوجد زر لتشغيل/إيقاف/فحص حالة/إعادة تشغيل،
أي أن `startCommand` و`stopCommand`… بيانات ميتة.

**الإصلاح:** `ServiceAction` + `ServiceViewModel.runAction()` وأزرار لكل إجراء مُعرَّف،
مع تسجيل النتيجة في السجل وعرضها في حوار موحّد.

### 1.5 كسر محتمل لقيد المفتاح الأجنبي في السجل
`ExecutionHistory.commandId` كان `Long` غير قابل للعدم مع `ForeignKey` على `Command`،
فأي تسجيل لا يخص أمرًا محفوظًا (مثل تنفيذ خدمة) كان يفشل مع `SQLiteConstraintException`.

**الإصلاح:** `commandId: Long?` + حقل `label` لعرض اسم الأمر/الخدمة، ورفع نسخة قاعدة البيانات
إلى 2 (مع `fallbackToDestructiveMigration` الموجود مسبقًا).

---

## 2) أخطاء واجهة المستخدم

| # | المشكلة | الإصلاح |
|---|---|---|
| 2.1 | تجاهل `paddingValues` في `Scaffold` بشواشي لوحة التحكم والأوامر والإعدادات → المحتوى يختفي تحت شريط العنوان | تمرير `paddingValues` إلى المحتوى في كل الشاشات |
| 2.2 | زر الرجوع ينفّذ `navigate("dashboard")` → تكديس لا نهائي للشاشات وزر رجوع النظام لا يعمل كما يُتوقع | `navigateBack()` المعتمِد على `popBackStack()` + مسارات موحّدة في `ui/navigation/Routes.kt` |
| 2.3 | قائمة اختيار البيئة يدوية (Column + clickable) بسلوك غير صحيح وطبقات ألوان معكوسة | `FilterChip` قياسي |
| 2.4 | لا توجد حالة فارغة ولا مؤشر تقدّم أثناء التنفيذ | رسائل حالة فارغة + `CircularProgressIndicator` / `LinearProgressIndicator` |
| 2.5 | الواجهة عربية بالكامل لكن الاتجاه يتبع لغة الجهاز | فرض `LayoutDirection.Rtl` في `MainActivity` |
| 2.6 | حقل «المهلة» يرمي `NumberFormatException` ولا يمكن مسحه | تصفية الأرقام + `keyboardType = Number` |
| 2.7 | تكرار كود حوار النتيجة في أكثر من شاشة | مكوّن مشترك `ui/components/ExecutionResultDialog.kt` |
| 2.8 | زر المفضلة كان أيقونة عرض فقط | زر فعلي `toggleFavorite` |
| 2.9 | سمة `@android:style/Theme.Material.NoActionBar` (داكنة) مع واجهة فاتحة → وميض/خلفية غير متطابقة | سمة خاصة `Theme.LinuxTermuxPanel` مع نسخة `values-night` |
| 2.10 | أيقونة التطبيق كانت `@android:drawable/ic_menu_manage` | أيقونة متجهية + Adaptive Icon |

---

## 3) كود ميت ومشاكل بنيوية

- أنشطة غير مسجّلة في المانيفست ولا تُستعمل: `DashboardActivity`, `ServicesActivity`,
  `HistoryActivity`, `SettingsActivity` (كل منها `@AndroidEntryPoint` يولّد كودًا بلا فائدة) → **حُذفت**.
- `ShellCommandExecutor` كان معرّفًا ولا يُستعمل → صار **خطة بديلة فعلية** عند غياب Termux.
- تكرار واجهة/تسمية مربكة: `CommandExecutor : TermuxCommandExecutor` يحقن `TermuxCommandExecutor`
  → إعادة هيكلة طبقة `execution` (`ExecutionResult` في ملف مستقل، حذف الواجهة الزائدة و`di/ExecutionModule`).
- استعلامات Room المفردة كانت `fun ...: Entity` غير معلّقة (تُنفَّذ على الخيط المستدعي وقد
  ترمي استثناء على الخيط الرئيسي) وتُعيد نوعًا غير قابل للعدم → صارت `suspend fun ...: Entity?`.
- `provider_paths.xml` و`FileProvider` بلا استخدام بعد إعادة كتابة التنفيذ → حُذفت.
- عشرات الاستيرادات غير المستخدمة (`Bundle`, `ComponentActivity`, `AndroidEntryPoint`, `sp` …) → نُظِّفت.

---

## 4) مشاكل البناء والإعداد (معوّقات)

| # | المشكلة | الإصلاح |
|---|---|---|
| 4.1 | `proguard-rules.pro` مذكور في `build.gradle` وغير موجود → `assembleRelease` يفشل | إنشاء الملف بقواعد Room/Hilt/Compose |
| 4.2 | `.gitignore` يحتوي `.local.properties` (اسم خاطئ) فكان `local.properties` قابلًا للرفع | تصحيحه وإضافة `.idea/`, `*.apk`, `.cxx/` … |
| 4.3 | `gradlew` سكربت مختصر يدويًا: لا يحترم `JAVA_HOME`، ينكسر مع المسارات ذات الفراغات، ولا يوجد `gradlew.bat` | سكربت آمن (`set -e`, اقتباس المسارات, `JAVA_HOME`) + إضافة `gradlew.bat` |
| 4.4 | `android.enableJetifier=true` دون حاجة (يُبطئ البناء) | تعطيله + تفعيل `parallel` و`caching` وزيادة الذاكرة و`nonTransitiveRClass` |
| 4.5 | غياب `kapt { correctErrorTypes true }` الموصى به مع Hilt | أُضيف |
| 4.6 | اعتماديات غير مستعملة: `appcompat`, `material`, `constraintlayout`, `coil-compose` | حُذفت (حجم أصغر وبناء أسرع) |
| 4.7 | CI ينزّل NDK 25 (≈1GB) بلا أي كود أصلي، ويطبع `ANDROID_NDK_HOME` غير المعرّف | استخدام `android-actions/setup-android`، حذف NDK، إضافة `concurrency`، تشغيل اختبارات الوحدة، والتحقق من بناء الإصدار |
| 4.8 | لا يوجد أي اختبار ولا ملف `README` | 6 اختبارات وحدة + `README.md` يشرح المتطلبات وخطوات `allow-external-apps` |

---

## 5) نتيجة التحقق (GitHub Actions)

```
✓ Run unit tests            (6/6 ناجحة)
✓ Build Debug APK
✓ Build Release APK (unsigned)
```

---

## 6) ملاحظات وقيود متبقية

1. **إلزامي لعمل التطبيق:** تفعيل `allow-external-apps=true` في `~/.termux/termux.properties`
   ثم `termux-reload-settings`، مع استخدام نسخة Termux من F-Droid/GitHub (نسخة Google Play قديمة).
2. الوضع التفاعلي (`needsInteractiveTerminal`) يفتح جلسة Termux ولا يمكن التقاط مخرجاته — هذا قيد في Termux نفسه.
3. الخيار `runInBackground` محفوظ في النموذج لكن التنفيذ يتم دائمًا في الخلفية عند عدم تفعيل الوضع التفاعلي.
4. `applicationId` ما زال `com.example.linuxtermuxpanel` — يُستحسن تغييره قبل النشر، كما يلزم إعداد
   توقيع (signingConfig) لبناء نسخة إصدار قابلة للتثبيت.
5. لا توجد ترحيلات (migrations) حقيقية لقاعدة البيانات؛ التحديث يمسح البيانات
   (`fallbackToDestructiveMigration`) وهو مقبول قبل النشر فقط.
