import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

// Read MAPS_API_KEY from local.properties (per-machine, not committed to source) rather than
// hardcoding it in the manifest, so the key doesn't sit directly in a source file.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "pk.kissanmadadgar.mobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "pk.kissanmadadgar.mobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")

        // App only ever displays Urdu text of its own; "en" is kept too since it's the
        // unqualified default locale most third-party libraries (Play Services, Firebase, etc.)
        // ship their strings under. This only drops OTHER languages' copies of their strings
        // (French, German, Hindi, ...) that this app never renders — not a behavior change.
        resourceConfigurations += listOf("en", "ur")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX & Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Retrofit & OkHttp Network Libraries
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Firebase Cloud Messaging (Android Push Notifications)
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation("com.google.firebase:firebase-messaging")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Security (Encrypted SharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
        
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Google Play In-App Updates
    implementation("com.google.android.play:app-update-ktx:2.1.0")

    // Google Maps (used for both the Search tab's map and the Farmer map tab, via
    // ClusteredMachineryGoogleMap in GoogleMachineryMap.kt — MapLibre was removed in favor of
    // standardizing on this single map engine, cutting its ~11MB/ABI native library entirely)
    // Pinned to a maps-compose generation compatible with this app's compose-bom (2023.10.01) —
    // 4.3.3 requires a newer Compose runtime and caused a NoSuchMethodError in Material3's
    // CircularProgressIndicator (animation-core got resolved to a mismatched newer version).
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("com.google.maps.android:maps-compose-utils:2.11.4")
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.zxing:core:3.5.3")

    // In-app video player (Media3 ExoPlayer) for the "how to use the app" helper videos — the
    // backend now serves them as direct MP4 files rather than YouTube links, so playback is
    // native ExoPlayer instead of an embedded YouTube WebView.
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
