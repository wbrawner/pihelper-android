buildscript {
    repositories {
        google()
        mavenCentral()
    }
    val coroutinesVersion by extra("1.3.2")
    val hiltVersion by extra("2.36")
    val koinVersion by extra("2.0.1")
    val kotlinVersion by extra("1.4.32")
    val okHttpVersion by extra("4.2.2")
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-beta03")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
    }
}
