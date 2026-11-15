-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.brainrush.app.**$$serializer { *; }
-keepclassmembers class com.brainrush.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.brainrush.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
