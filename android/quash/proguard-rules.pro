# Keep specific classes and class members
-keepclassmembers class com.quash.bugs.Quash {
    public <methods>;
}

# Apply the class name dictionary for obfuscation
-classobfuscationdictionary 'class-names-dictionary.txt'

# Repackage all obfuscated classes into a unique package
-repackageclasses 'com.quash.obfuscated'

# Retain service method parameters when optimizing
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore warnings for certain classes and annotations
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Keep Retrofit interfaces and their methods, allow obfuscation of their names
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services and allow obfuscation
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# Keep certain Kotlin-specific classes and allow obfuscation
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep generic signatures for Retrofit return types and allow obfuscation
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# Keep Retrofit Response class and allow obfuscation
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**

-keep class com.jakewharton.retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn com.jakewharton.retrofit.**

-keep class com.google.firebase.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-dontwarn com.google.firebase.**
