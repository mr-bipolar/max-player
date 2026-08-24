# Keep all your app classes (VERY IMPORTANT for MPV source)
-keep class com.aaronmaxlab.maxplayer.** { *; }

# Keep native methods (MPV uses native .so)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Activities
-keep class * extends android.app.Activity

# Keep Views
-keep class * extends android.view.View { *; }

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# AdMob (if you use ads)
-keep class com.google.android.gms.ads.** { *; }