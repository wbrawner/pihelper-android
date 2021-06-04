plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdk = libs.versions.maxSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.maxSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("io.insert-koin:koin-core:${libs.versions.koin.get()}")
    implementation("com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}")
    implementation("com.squareup.okhttp3:logging-interceptor:${libs.versions.okhttp.get()}")
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlin.reflect)
    implementation(libs.moshi.core)
    kapt(libs.moshi.codegen)
    testImplementation(libs.junit)
}
