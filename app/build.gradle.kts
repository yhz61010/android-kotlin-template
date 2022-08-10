import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig

plugins {
    id(GradlePluginId.ANDROID_APPLICATION)
    id(GradlePluginId.KOTLIN_ANDROID) // or kotlin("android") or id 'kotlin-android'
    id(GradlePluginId.KOTLIN_KAPT) // or kotlin("kapt")
//    apply plugin: 'kotlin-parcelize'
    id(GradlePluginId.KTLINT_GRADLE)
    id(GradlePluginId.SAFE_ARGS)
    id(GradlePluginId.ANDROID_JUNIT_5)
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
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
        }

        getByName(BuildType.DEBUG) {
            isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
        }
    }
    lint {
        warningsAsErrors = true
        abortOnError = true
    }
    lintOptions {
        // By default lint does not check test sources, but setting this option means that lint will not even parse them
        isIgnoreTestSources = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation (libs.bundles.kotlin)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.coroutines)
    implementation(libs.recyclerview)
    implementation(libs.constraintLayout)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.lifecycle)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.junit.jupiter.engine)
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
