import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

// ✅ Безопасное чтение api.properties
private val apiProperties = Properties().apply {
    val file = rootProject.file("api.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

private val apiKey: String = apiProperties.getProperty("NEWS_API_KEY") ?: ""

android {
    namespace = "com.dron.news"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.dron.news"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NEWS_API_KEY", apiKey)
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.databinding.adapters)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose Material Icons Extended (ЭТО ВАЖНО ДЛЯ icons!)
    implementation(libs.androidx.compose.material.icons.extended)

    // Jetpack Compose Integration
    implementation (libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)
    implementation("com.squareup.okhttp3:logging-interceptor:5.5.0")
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    // When using Kotlin.
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.datastore.preferences)


    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}