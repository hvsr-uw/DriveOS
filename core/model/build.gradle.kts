plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rivian.driveos.model"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
    }
}
