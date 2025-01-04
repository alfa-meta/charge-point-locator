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
        // Add the API key as a BuildConfig field
        val mapsApiKey: String = project.findProperty("MAPS_API_KEY") as String? ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")

        manifestPlaceholders["MAPS_API_KEY"] = project.findProperty("MAPS_API_KEY") ?: ""
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