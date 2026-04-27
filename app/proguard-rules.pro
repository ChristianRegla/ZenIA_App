# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve the line number information for debugging stack traces in Play Console / Crashlytics
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to hide the original source file name.
#-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------
# REGLAS PARA FIREBASE FIRESTORE Y MODELOS DE DATOS
# ------------------------------------------------------------------

# Mantener firmas y anotaciones (necesario para que Firestore lea los datos)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# PROTEGER TODOS LOS MODELOS DE DATOS DE ZENIA
# Esto evita que R8 renombre las clases y borre los constructores vacíos que usa Firebase
-keep class com.zenia.app.model.** { *; }