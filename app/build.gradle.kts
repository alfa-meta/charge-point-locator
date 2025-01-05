import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.chargepointlocator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.chargepointlocator"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildFeatures {
            buildConfig = true
        }

        // Load MAPS_API_KEY from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream ->
                localProperties.load(stream)
            }
        }

        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")

        //Pass the API key to Manifest
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
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
}

dependencies {
    implementation(libs.material.v190)
    implementation(libs.play.services.maps.v1810)
    implementation(libs.play.services.maps)
    implementation(libs.drawerlayout)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment)
    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    testImplementation(libs.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}