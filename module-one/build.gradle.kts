// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.library)
//    alias(libs.plugins.kotlin.android)
//    // id("org.jetbrains.kotlin.kapt") // or kotlin("kapt")
//    // If you use kotlin(), you can change dash(-) with dot(.)
//    // or you can still use dash like id("kotlin-parcelize")
//    alias(libs.plugins.kotlin.kapt)
//    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    // https://developer.android.com/reference/tools/gradle-api/7.1/com/android/build/api/dsl/Lint
    lint {
        // if true, stop the gradle build if errors are found
        abortOnError = true
        // Like checkTestSources, but always skips analyzing tests -- meaning that it
        // also ignores checks that have explicitly asked to look at test sources, such
        // as the unused resource check.
        ignoreTestSources = true

        // turn off checking the given issue id's
        disable += setOf(
            // "MissingTranslation",
            // "GoogleAppIndexingWarning",
            "RtlHardcoded",
            "RtlCompat",
            "RtlEnabled"
        )
    }

//    buildFeatures {
//        viewBinding = true
//    }
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.test)
}
