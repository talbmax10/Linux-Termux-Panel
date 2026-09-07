# قواعد ProGuard/R8 للمشروع.
# التصغير معطّل حاليًا (minifyEnabled false) لكن الملف مطلوب حتى لا يفشل بناء الإصدار،
# وهذه القواعد جاهزة عند تفعيل التصغير لاحقًا.

# الحفاظ على أسماء الكيانات المستخدمة مع Room
-keep class com.example.linuxtermuxpanel.data.model.** { *; }

# Hilt / Dagger
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin coroutines
-dontwarn kotlinx.coroutines.**

# Compose
-dontwarn androidx.compose.**
