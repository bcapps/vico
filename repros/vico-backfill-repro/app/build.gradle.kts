import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val composeVersion = "1.11.0-beta01"
val composeMaterial3Version = "1.11.0-alpha05"

android {
    namespace = "com.example.vicobackfillrepro"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.vicobackfillrepro"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("com.patrykandpatrick.vico:compose:3.1.0")
    implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
    implementation("org.jetbrains.compose.material3:material3:$composeMaterial3Version")
    implementation("org.jetbrains.compose.ui:ui:$composeVersion")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:$composeVersion")

    debugImplementation("org.jetbrains.compose.ui:ui-tooling:$composeVersion")
}
