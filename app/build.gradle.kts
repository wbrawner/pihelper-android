import java.util.Properties
import java.io.FileInputStream
import java.io.FileNotFoundException

plugins {
    id("com.android.application")
    id("kotlin-android")
}

val keystoreProperties = Properties()
try {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
} catch (ignored: FileNotFoundException) {
    logger.warn("Unable to load keystore properties. Using debug signing configuration instead")
    keystoreProperties["keyAlias"] = "androiddebugkey"
    keystoreProperties["keyPassword"] = "android"
    keystoreProperties["storeFile"] =
        File(System.getProperty("user.home"), ".android/debug.keystore").absolutePath
    keystoreProperties["storePassword"] = "android"
}

android {
    compileSdk = 29
    defaultConfig {
        applicationId = "com.wbrawner.pihelper"
        minSdk = 23
        targetSdk = 29
        versionCode = 2
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs["debug"]
    }
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"].toString()
            keyPassword = keystoreProperties["keyPassword"].toString()
            storeFile = file(keystoreProperties["storeFile"].toString())
            storePassword = keystoreProperties["storePassword"].toString()
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs["release"]
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":piholeclient"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${rootProject.ext["kotlinVersion"]}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.ext["coroutinesVersion"]}")
    implementation("org.koin:koin-androidx-viewmodel:${rootProject.ext["koinVersion"]}")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.3.0-alpha04")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.security:security-crypto:1.0.0-rc01")
    implementation("androidx.preference:preference-ktx:1.1.1")
    testImplementation("junit:junit:4.13.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    val navigation_version = "2.3.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigation_version")
    implementation("androidx.navigation:navigation-ui-ktx:$navigation_version")
    val lifecycle_version = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
}

