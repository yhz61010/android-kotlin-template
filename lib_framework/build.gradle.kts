apply(from = "../jacoco.gradle.kts")

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)

    // alias(libs.plugins.ksp)
    // alias(libs.plugins.hilt)

    alias(libs.plugins.android.junit5)

    alias(libs.plugins.sonarqube)
    jacoco
}

android {
    namespace = "com.leovp.framework"

    resourcePrefix = "frm_"

    // https://medium.com/androiddevelopers/5-ways-to-prepare-your-app-build-for-android-studio-flamingo-release-da34616bb946
    buildFeatures {
        // dataBinding = true
        // viewBinding is enabled by default. Check [build.gradle.kts] in the root folder of project.
        // viewBinding = true
        // aidl = true
        // Add this line as needed
        buildConfig = true
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
    // implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    // ----------
    api(libs.mars.xlog)
    api(libs.karn.notify)
    api(libs.mmkv)
    api(libs.serialization.json)
    // Net - dependencies - Start
    api(libs.kotlin.coroutines)
    api(libs.square.okhttp)
    api(libs.net)
    // Net - dependencies - End
    // ----------

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

    api(libs.bundles.kotlin)

    api(libs.leo.androidbase)
    api(libs.leo.pref)
    api(libs.leo.log)
    api(libs.leo.lib.json)

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
