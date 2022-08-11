import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig

// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt) // id("org.jetbrains.kotlin.kapt") // or kotlin("kapt")
    alias(libs.plugins.kotlin.parcelize) //  id("kotlin-parcelize")

    alias(libs.plugins.navigation)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = AndroidConfig.ID
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION_NAME

        testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER

        // buildConfigFieldFromGradleProperty("apiBaseUrl")
        // buildConfigField("FEATURE_MODULE_NAMES", getFeatureNames())
    }

    buildTypes {
        getByName(BuildType.RELEASE) {
            isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
        }
    }

    // https://developer.android.com/reference/tools/gradle-api/7.1/com/android/build/api/dsl/Lint
    lint {
        // if true, stop the gradle build if errors are found
//        abortOnError = true
        // Like checkTestSources, but always skips analyzing tests -- meaning that it
        // also ignores checks that have explicitly asked to look at test sources, such
        // as the unused resource check.
//        ignoreTestSources = true

        // turn off checking the given issue id's
        disable.apply {
//            add("MissingTranslation")
            add("RtlHardcoded")
            add("RtlCompat")
            add("RtlEnabled")
        }
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
}

dependencies {
    implementation(libs.android.material)
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.bundles.navigation)

//    implementation(libs.bundles.navigation)
//    implementation(libs.bundles.lifecycle)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
}

/*
Takes value from Gradle project property and sets it as build config property
 */
fun BaseFlavor.buildConfigFieldFromGradleProperty(gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    checkNotNull(propertyValue) { "Gradle property $gradlePropertyName is null" }

    val androidResourceName = "GRADLE_${gradlePropertyName.toSnakeCase()}".toUpperCase()
    buildConfigField("String", androidResourceName, propertyValue)
}

fun String.toSnakeCase() = this.split(Regex("(?=[A-Z])")).joinToString("_") { it.toLowerCase() }

/*
Adds a new field to the generated BuildConfig class
 */
fun DefaultConfig.buildConfigField(name: String, value: Array<String>) {
    // Create String that holds Java String Array code
    val strValue = value.joinToString(prefix = "{", separator = ",", postfix = "}", transform = { "\"$it\"" })
    buildConfigField("String[]", name, strValue)
}
