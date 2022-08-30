apply(from = "../jacoco.gradle.kts")

// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.library)

    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")

    alias(libs.plugins.sonarqube)
    jacoco
}

android {
    namespace = "com.leovp.module.common"

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

dependencies {
    implementation(libs.bundles.kotlin)

    api(libs.leo.androidbase)
    api(libs.leo.pref)
    api(libs.leo.log)
    api(libs.leo.floatview)
    api(libs.leo.lib.json)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.bundles.test.runtime.only)
    androidTestImplementation(libs.bundles.android.test)
}
