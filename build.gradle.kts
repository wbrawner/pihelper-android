import java.net.URI

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.bundles.plugins)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = URI("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
    }
}