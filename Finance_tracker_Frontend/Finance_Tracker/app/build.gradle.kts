import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    // Reads app/google-services.json (gitignored; written from the GOOGLE_SERVICES_JSON Codespaces secret)
    alias(libs.plugins.google.services)
}

// Debug builds read the backend URL from local.properties (gitignored, so a Codespace address is
// never committed), e.g. financeTracker.baseUrl=https://<codespace>-8080.app.github.dev/
// Release builds keep the placeholder until deployment.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val placeholderBaseUrl = "https://api.yourdomain.com/"
val debugBaseUrl = (localProperties.getProperty("financeTracker.baseUrl") ?: placeholderBaseUrl)
    .trim()
    .let { if (it.endsWith("/")) it else "$it/" } // Retrofit requires a trailing slash

android {
    namespace = "com.example.finance_tracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.finance_tracker"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$placeholderBaseUrl\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        // Robolectric (Room tests on the JVM) reads the merged Android resources
        unitTests.isIncludeAndroidResources = true
    }

    packaging {
        resources {
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.viewmodel.lifecycle)
    implementation(libs.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.okhttp)

    // Data Storage
    implementation(libs.datastore)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt
    implementation(libs.hilt)
    implementation(libs.dagger)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines)

    // Push notifications (Firebase Cloud Messaging)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    // Compose UI tests on the JVM via Robolectric (ComponentActivity comes from ui-test-manifest below)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

configurations.all {
    exclude(group = "xpp3", module = "xpp3")
    exclude(group = "com.intellij", module = "annotations")
}