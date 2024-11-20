buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Add the Google Services plugin here
        classpath("com.google.gms:google-services:4.3.15")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
