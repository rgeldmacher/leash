-keep class com.rgeldmacher.leash.** { *; }
-keep class **Leash { *; }
-keepclasseswithmembernames class * {
    @com.rgeldmacher.leash.* <fields>;
}
-keepclasseswithmembernames class * {
    @com.rgeldmacher.leash.* <methods>;
}
-dontwarn com.rgeldmacher.leash.**
-dontwarn javax.**
-dontwarn java.io.**
-dontwarn java.nio.**
