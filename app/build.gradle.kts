plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android { namespace = "com.example.localhtmlpreview"; compileSdk = 35
    defaultConfig { applicationId = "com.example.localhtmlpreview"; minSdk = 29; targetSdk = 35; versionCode = 1; versionName = "1.0" }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.9.2")
}

kotlin { jvmToolchain(17) }
