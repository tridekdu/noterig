plugins {
    kotlin("multiplatform") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.0"
    id("com.android.application") version "8.5.2"   // was 8.5.2
}
kotlin {
    jvmToolchain(21)
    androidTarget()
    jvm("desktop")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.9.2")
                implementation("androidx.compose.ui:ui:1.7.2")
                implementation("androidx.compose.material3:material3:1.3.0")
                //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
                implementation("androidx.core:core-ktx:1.13.1")
                // Boox / Onyx SDK
                //implementation("com.onyx.android.sdk:onyxsdk-device:1.1.11")
                //implementation("com.onyx.android.sdk:onyxsdk-pen:1.2.1")
                //implementation("com.onyx.android.sdk:onyxsdk-pen:1.5.0.4")
                //implementation("com.onyx.android.sdk:onyxsdk-device:1.8.2.1")   // override bad 1.5.0.2
                //implementation("com.onyx.android.sdk:onyxsdk-geometry:1.1.0.4") // satisfy transitive
                implementation("com.onyx.android.sdk:onyxsdk-pen:1.4.12.1") {
                    // drop old support libs that clash with AndroidX
                    exclude(group = "com.android.support", module = "support-compat")
                    exclude(group = "com.android.support", module = "support-annotations")
                    exclude(group = "com.android.support", module = "support-core-utils")
                    exclude(group = "com.android.support", module = "support-core-ui")
                }
                implementation("com.onyx.android.sdk:onyxsdk-base:1.8.2.1")
                implementation("com.onyx.android.sdk:onyxsdk-device:1.3.1.3")
                //implementation("com.onyx.android.sdk:onyxsdk-geometry:1.1.0.4")
            }
        }
    }
}
compose.desktop { application { mainClass = "app.notegamut.DesktopMainKt" } }
android { namespace = "app.notegamut"; compileSdk = 35
    defaultConfig { applicationId = "app.notegamut"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
        jniLibs {
            pickFirsts += listOf(
                "lib/arm64-v8a/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so",
                "lib/x86/libc++_shared.so",
                "lib/x86_64/libc++_shared.so"
            )
        }
    }
}
