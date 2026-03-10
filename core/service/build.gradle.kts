plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rivian.driveos.service"
    compileSdk = 35

    defaultConfig {
        minSdk = 28
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}
