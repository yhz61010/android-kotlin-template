import java.io.ByteArrayOutputStream

// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // id("org.jetbrains.kotlin.kapt") // or kotlin("kapt")
    // If you use kotlin(), you can change dash(-) with dot(.)
    // or you can still use dash like id("kotlin-parcelize")
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")

    alias(libs.plugins.navigation)

    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    compileSdk = libs.versions.compile.sdk.get().toInt()

    val appName = "LeoTemplate"

    defaultConfig {
        applicationId = namespace
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        multiDexEnabled = true

//        ndk {
//            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
//        }

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
//        resources.excludes += setOf(
        resources.pickFirsts += setOf(
            "META-INF/AL2.0",
            "META-INF/licenses/**",
            "META-INF/LGPL2.1",
            "META-INF/atomicfu.kotlin_module",
            "META-INF/NOTICE",
            "META-INF/NOTICE.*",
            "META-INF/DEPENDENCIES",
            "META-INF/DEPENDENCIES.*",
            "META-INF/LICENSE",
            "META-INF/LICENSE.*",
            "META-INF/INDEX.LIST",
            "META-INF/io.netty.versions.properties",
        )
    }

    applicationVariants.all {
        val variant = this
        variant.outputs
            .mapNotNull { it as? com.android.build.gradle.internal.api.ApkVariantOutputImpl }
            .forEach { output ->
                variant.packageApplicationProvider.get().outputDirectory
                output.outputFileName = "${appName}${("-$flavorName").takeIf { it != "-" } ?: ""}-${buildType.name}" +
                        "-v$versionName($versionCode)" +
                        "-${gitVersionTag()}-${gitCommitCount()}" +
//                        ("-unaligned".takeIf { !output.zipAlign.enabled } ?: "") +
                        ".apk"
            }
    }
}

// 获取当前分支的提交总次数
fun gitCommitCount(): Int {
//    val cmd = 'git rev-list HEAD --first-parent --count'
    val cmd = "git rev-list HEAD --count"

    val stdout = ByteArrayOutputStream()
    runCatching {
        exec {
            commandLine = cmd.trim().split(' ')
            standardOutput = stdout
        }
    }.getOrDefault(0)
    // You must trim() the result. Because the result of command has a suffix '\n'.
    return stdout.toString().trim().toInt()
}

// 使用commit的哈希值作为版本号也是可以的，获取最新的一次提交的哈希值的前七个字符
// $ git rev-list HEAD --abbrev-commit --max-count=1
// a935b078

/*
 * 获取最新的一个tag信息
 * $ git describe --tags
 * 4.0.4-9-ga935b078
 * 说明：
 * 4.0.4        : tag名
 * 9            : 打tag之后又有四次提交
 * ga935b078    ：开头 g 为 git 的缩写，在多种管理工具并存的环境中很有用处
 * a935b078     ：当前分支最新的 commitID 前几位
 */

fun gitVersionTag(): String {
    // https://stackoverflow.com/a/4916591/1685062
//    val cmd = "git describe --tags"
    val cmd = "git describe --always"

    val stdout = ByteArrayOutputStream()
    runCatching {
        exec {
            commandLine = cmd.trim().split(' ')
            standardOutput = stdout
        }
    }.getOrDefault(null) ?: return "NA"
    var versionTag = stdout.toString().trim()

    val regex = "-(\\d+)-g".toRegex()
    val matcher: MatchResult? = regex.matchEntire(versionTag)

    val matcherGroup0: MatchGroup? = matcher?.groups?.get(0)
    versionTag = if (matcher?.value?.isNotBlank() == true && matcherGroup0?.value?.isNotBlank() == true) {
        versionTag.substring(0, matcherGroup0.range.first) + "." + matcherGroup0.value
    } else {
        versionTag
    }

    return versionTag
}

dependencies {
    implementation(libs.android.material)
    implementation(libs.bundles.kotlin)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.lifecycle)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
}
