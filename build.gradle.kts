buildscript {
    repositories {
        google()
        mavenCentral()
    }
    val kotlinVersion by extra("1.4.32")
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-beta03")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
    }
}
