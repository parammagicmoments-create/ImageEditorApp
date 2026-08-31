# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

# Optimizations
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Keep our classes
-keep class com.example.picflow.** { *; }
-keep class com.example.picflow.MainActivity { *; }
-keep class com.example.picflow.EditActivity { *; }

# Keep AndroidX
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Keep Material Design
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

# Keep all native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom application classes that might be instantiated by name
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
