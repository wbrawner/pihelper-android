buildscript {
    repositories {
        google()
        mavenCentral()
    }
    val hiltVersion by extra("2.36")
    val kotlinVersion by extra("1.4.32")
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-beta03")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
    }
}
