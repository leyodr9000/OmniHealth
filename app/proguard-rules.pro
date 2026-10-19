# OmniHealth ProGuard rules

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.example.omnihealth.**$$serializer { *; }
-keepclassmembers class com.example.omnihealth.** { *** Companion; }
-keepclasseswithmembers class com.example.omnihealth.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
