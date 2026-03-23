# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ── Atributos generales ──────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ── Room ─────────────────────────────────────────────────────────────────────
-keep class com.i3dcor.scanbook.data.local.entity.** { *; }
-keep interface com.i3dcor.scanbook.data.local.dao.** { *; }
# La regla consumer de Room solo protege la clase, no el constructor.
# R8 elimina <init>() de la _Impl generada; la clase se instancia por reflexión.
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# ── Retrofit + interfaces de red ─────────────────────────────────────────────
-keep interface com.i3dcor.scanbook.data.network.GoogleBooksApi { *; }
-keep interface com.i3dcor.scanbook.data.network.OpenLibraryApi { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ── Gson / DTOs de red ───────────────────────────────────────────────────────
-keep class com.i3dcor.scanbook.data.network.dto.** { *; }
-keepclassmembers class com.i3dcor.scanbook.data.network.dto.** {
    <init>();
    <fields>;
}

# ── WorkManager ──────────────────────────────────────────────────────────────
-keep class com.i3dcor.scanbook.data.worker.DownloadCoverWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ── androidx.startup (Initializer se instancia por reflexión) ────────────────
-keep class * implements androidx.startup.Initializer {
    <init>();
}
-keepnames class androidx.startup.Initializer

# ── Modelos de dominio ───────────────────────────────────────────────────────
-keep class com.i3dcor.scanbook.domain.model.** { *; }

# ── OkHttp ───────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Kotlin coroutines ────────────────────────────────────────────────────────
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
