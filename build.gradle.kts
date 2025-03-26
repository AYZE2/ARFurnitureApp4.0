buildscript {
    // Define kotlin version using extra property accessor
    val kotlin_version by extra("1.9.0")

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        // Other classpath dependencies
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}