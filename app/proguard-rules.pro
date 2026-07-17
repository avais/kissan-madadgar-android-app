# Preserve generic signatures / annotations that Retrofit, Gson and Room all rely on
# reflectively at runtime — stripping these doesn't fail the build, it silently breaks JSON
# parsing / network deserialization in the shipped app.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJavaVersion
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Retrofit's official R8 full-mode rules (AGP 8 defaults to full mode, which is stricter than the
# compat mode these were less critical under). Without these, R8 sees no real subtype of a
# Retrofit service interface (it's only ever implemented via a runtime Proxy) and strips/erases
# the generic signature Retrofit needs to reflect on for suspend-fun return types — which is
# exactly what caused every API call to throw "Class cannot be cast to ParameterizedType" and
# fail before a single HTTP request went out.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * extends <1>
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Belt-and-suspenders: also keep our own service interfaces and their DTOs outright.
# Must be "class", not "interface" — this package holds AuthApiService (an interface) AND its
# request/response data classes (AndroidNotificationDto, PushTokenRequest, etc.). "-keep interface"
# only matches interface types, silently leaving those data classes unprotected; R8 was then free
# to rename their fields in release builds, which Gson's Unsafe-based deserialization can't detect,
# so non-null Kotlin fields (e.g. AndroidNotificationDto.title) came back null and crashed on first
# use — reproducible only in the minified release build, never in a debug run. "class" in ProGuard's
# grammar matches classes AND interfaces (unlike "interface", which restricts to interfaces only),
# so this single rule now covers both without needing a second line.
-keep class pk.kissanmadadgar.mobile.data.remote.api.** { *; }

# Gson maps JSON keys to field names (via @SerializedName or the field name itself) purely via
# reflection — renaming/stripping a field is invisible at compile time and breaks parsing only
# at runtime. Keep every field on every model Gson ever touches.
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep class pk.kissanmadadgar.mobile.data.remote.dto.** { *; }
-keep class pk.kissanmadadgar.mobile.domain.model.** { *; }
-keep class pk.kissanmadadgar.mobile.data.local.entity.** { *; }

# Gson (de)serializes enums via name() / valueOf(), and SessionManager/BookingRepositoryImpl
# call UserRole.valueOf()/BookingStatus.valueOf() directly — keep the standard enum machinery.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Room's own consumer rules cover entities/DAOs generated via KSP; this only needs to protect
# our Gson-based type converters (KissanConverters.kt) from renaming fields inside stored JSON.
-keep class pk.kissanmadadgar.mobile.data.local.converter.** { *; }

# google-maps-android-utils' clustering algorithm resolves some helper classes reflectively.
-keep class com.google.maps.android.** { *; }
