@file:Suppress(
    "ktlint:standard:max-line-length", // for ktlint
    "MaximumLineLength", // for detekt
    "MaxLineLength", // for detekt
    "LongLine", // for detekt
)

apply(from = "../jacoco.gradle.kts")

// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.library)
    // Apply the `compose.compiler` plugin to every module that uses Jetpack Compose.
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")

    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)

    alias(libs.plugins.android.junit5)

    alias(libs.plugins.sonarqube)
    jacoco
}

android {
    namespace = "com.leovp.feature.base"

    resourcePrefix = "bas_"

    // https://medium.com/androiddevelopers/5-ways-to-prepare-your-app-build-for-android-studio-flamingo-release-da34616bb946
    buildFeatures {
        // dataBinding = true
        // viewBinding is enabled by default. Check [build.gradle.kts] in the root folder of project.
        // viewBinding = true
        // aidl = true
        // Add this line as needed
        buildConfig = true
    }

    defaultConfig {
        // Connect JUnit 5 to the runner
        testInstrumentationRunnerArguments["runnerBuilder"] =
            "de.mannodermaus.junit5.AndroidJUnit5Builder"

        // buildConfigFieldFromGradleProperty("apiBaseUrl")
        // buildConfigFieldFromGradleProperty("apiToken")

        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
    }

    buildTypes {
        debug {
            // getByName("debug")
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("boolean", "CONSOLE_LOG_OPEN", "true")
        }

        release {
            // getByName("release")
            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("boolean", "CONSOLE_LOG_OPEN", "false")
        }
    }

    // https://developer.android.com/reference/tools/gradle-api/7.1/com/android/build/api/dsl/Lint
    lint {
        // if true, stop the gradle build if errors are found
        abortOnError = false
        // Like checkTestSources, but always skips analyzing tests -- meaning that it
        // also ignores checks that have explicitly asked to look at test sources, such
        // as the unused resource check.
        ignoreTestSources = true
    }
}

ksp {
    // The following two lines will suppress the warning:
    // Hilt_CustomApplication.java:25: 警告: [deprecation] Builder中的applicationContextModule(ApplicationContextModule)已过时
    //           .applicationContextModule(new ApplicationContextModule(Hilt_CustomApplication.this))
    arg("dagger.fastInit", "enabled")
    arg("dagger.formatGeneratedSource", "disabled")
}

composeCompiler {
    // deprecated
    // enableStrongSkippingMode = true
    // featureFlags.addAll(ComposeFeatureFlag.StrongSkipping, ComposeFeatureFlag.OptimizeNonSkippingGroups)
    includeSourceInformation = true
}

dependencies {
    // api(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    api(platform(libs.androidx.compose.bom))
    // Material Design 3
    api(libs.androidx.material3)
    api(libs.bundles.androidx.compose)
    // Android Studio Preview support
    api(libs.androidx.compose.ui.tooling.preview)
    // api(libs.androidx.compose.ui.graphics)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    api(libs.lifecycle.runtime.compose)

    // ----------
    api(libs.leo.lib.compose)
    api(libs.leo.mvvm)
    api(libs.leo.lib.network)
    api(libs.leo.floatview)
    api(libs.leo.androidbase)
    api(libs.leo.pref)
    api(libs.leo.log)
    api(libs.leo.lib.json)
    api(libs.leo.lib.common.android)

    api(libs.bundles.kotlin)
    api(libs.kotlin.coroutines)
    api(libs.karn.notify)
    api(libs.coil.kt.compose)
    api(libs.mars.xlog)
    api(libs.mmkv)
    api(libs.serialization.json)
    // api(libs.lottie.compose)
    // Net - dependencies - Start
    api(libs.kotlin.coroutines)
    api(libs.square.okhttp)
    api(libs.net)
    // Net - dependencies - End

    // Hilt - start
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    // implementation(libs.ksp.symbol.processing.api)
    ksp(libs.hilt.compiler)
    // Hilt - end
    // ----------

    // ==============================
    testImplementation(libs.bundles.test)
    // testRuntimeOnly(libs.bundles.test.runtime.only)
    // androidTestImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // ==============================
    // The instrumentation test companion libraries
    // https://github.com/mannodermaus/android-junit5
    // ==============================
    androidTestImplementation(libs.mannodermaus.junit5.core)
    androidTestRuntimeOnly(libs.mannodermaus.junit5.runner)
    // ==============================
}
