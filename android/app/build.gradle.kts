plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Enable after adding your own app/google-services.json:
    // id("com.google.gms.google-services")
}

android {
    namespace = "com.nex.football"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nex.football"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Only needed for push notifications (optional):
    // implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    // implementation("com.google.firebase:firebase-messaging")
}
