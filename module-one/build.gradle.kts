// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // id("org.jetbrains.kotlin.kapt") // or kotlin("kapt")
    // If you use kotlin(), you can change dash(-) with dot(.)
    // or you can still use dash like id("kotlin-parcelize")
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")

    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()

        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packagingOptions {
        resources.excludes += setOf(
            "META-INF/{AL2.0,LGPL2.1}",
        )
        resources.pickFirsts += setOf(
            "META-INF/licenses/**",
            "META-INF/atomicfu.kotlin_module",
            "META-INF/NOTICE*",
            "META-INF/NOTICE*.*",
            "META-INF/DEPENDENCIES*",
            "META-INF/DEPENDENCIES*.*",
            "META-INF/LICENSE*",
            "META-INF/LICENSE*.*",
            "META-INF/INDEX.LIST",
            "META-INF/io.netty.versions.properties",
            "**/*.proto",
            "**/*.bin",
            "**/*.java",
            "**/*.properties",
            "**/*.version",
            "**/*.*_module",
            "*.txt",
            "kotlin/**",
            "kotlinx/**",
            "okhttp3/**",
            "META-INF/services/**",
        )
    }
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.test)
}
