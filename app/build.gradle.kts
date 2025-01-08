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

        val salt = localProperties.getProperty("SALT") ?: ""
        buildConfigField("String", "SALT", "\"$salt\"")


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
    // Core Libraries
    implementation(libs.material.v190)
    implementation(libs.play.services.maps.v1810)
    implementation(libs.play.services.maps)
    implementation(libs.drawerlayout)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment)
    implementation(libs.appcompat)
    implementation(libs.material)

    // Unit Testing Libraries
    testImplementation(libs.junit) // JUnit for Unit Tests
    testImplementation(libs.core) // Core Testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.ext.junit) // Mockito for Unit Tests

    // Android Instrumentation Testing Libraries
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
}
