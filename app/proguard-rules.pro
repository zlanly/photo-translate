# Proguard rules for Photo Translate App
# Generated for ML Kit, CameraX, Hilt, and Compose compatibility

# Keep Hilt classes
-keep class * {@Inject}
-keep class * {@Named}
-keep class * {@Component}
-keep class * {@Singletons}
-keep class * {@Provides}
-keep class * {@RetainBuildField}
-keepclassmembers class * {
    @com.google.dagger.hilt.android.BindView *;
}
-keepclassmembers class * {
    @com.google.dagger.hilt.android.EntryPoint *;
}

# Keep Room entities
-keepclassmembers * extends androidx.room.Entity {
    *;
}

# ML Kit doesn't need additional rules, but keep these safe
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# CameraX
-keep class androidx.camera.** { *; }
-keep class androidx.core.content.** { *; }

# OkHttp (transitive dependency of some ML Kit components)
-keep class okio.** { *; }
-keep class okhttp3.** { *; }

# Kotlin coroutines
-keepclassmembers class * {
    @kotlinx.coroutines.* *;
}

# AndroidX annotations
-keep class androidx.annotation.** { *; }

# General AndroidX keeps
-keep class androidx.** { !inner class; }
-keep class androidx.** { *; }

# Proguard for Google Services
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.internal.**

# Remove these warnings - they're safe to ignore
-dontwarn androidx.camera.**
-dontwarn androidx.core.content.**
-dontwarn org.jetbrains.kotlin.**
-dontwarn com.google.mlkit.**
