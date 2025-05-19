import com.android.build.api.dsl.LibraryDefaultConfig
import java.util.Locale

apply(from = "../jacoco.gradle.kts")

// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.library)
    // Apply the `compose.compiler` plugin to every module that uses Jetpack Compose.
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")

    alias(libs.plugins.ksp)

    alias(libs.plugins.android.junit5)

    alias(libs.plugins.sonarqube)
    jacoco
}

android {
    namespace = "com.leovp.module.common"

    resourcePrefix = "cmn_"

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
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"

        // buildConfigFieldFromGradleProperty("apiBaseUrl")
        // buildConfigFieldFromGradleProperty("apiToken")

        buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
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

    buildTypes {
        debug /*getByName("debug")*/ {
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("boolean", "CONSOLE_LOG_OPEN", "true")
        }

        release /*getByName("release")*/ {
            buildConfigField("boolean", "DEBUG_MODE", "false")
            buildConfigField("boolean", "CONSOLE_LOG_OPEN", "false")
        }
    }
}

composeCompiler {
    // deprecated
    // enableStrongSkippingMode = true
    // featureFlags.addAll(ComposeFeatureFlag.StrongSkipping, ComposeFeatureFlag.OptimizeNonSkippingGroups)
    includeSourceInformation = true
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    // Material Design 3
    api(libs.androidx.material3)
    api(libs.bundles.androidx.compose)
    // Android Studio Preview support
    api(libs.androidx.compose.ui.tooling.preview)
    // api(libs.androidx.compose.ui.graphics)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    api(libs.androidx.lifecycle.runtime.compose)
    // ----------

    // hilt - start
    // implementation(libs.hilt.android)
    // implementation(libs.androidx.hilt.navigation.compose)
    // // implementation(libs.ksp.symbol.processing.api)
    // ksp(libs.hilt.compiler)
    // hilt - end

    implementation(libs.bundles.kotlin)

    api(libs.leo.androidbase)
    api(libs.leo.pref)
    api(libs.leo.log)
    api(libs.leo.floatview)
    api(libs.leo.lib.json)

    api(libs.mars.xlog)
    api(libs.karn.notify)

    // ----------
    api(libs.androidx.navigation.compose)
    api(libs.androidx.compose.material.iconsExtended)

    // ==============================
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.bundles.test.runtime.only)
    androidTestImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.android.test)
    // ==============================
    // The instrumentation test companion libraries
    androidTestImplementation(libs.mannodermaus.junit5.core)
    androidTestRuntimeOnly(libs.mannodermaus.junit5.runner)
    // ==============================
}
