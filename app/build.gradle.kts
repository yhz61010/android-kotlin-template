import java.io.ByteArrayOutputStream
import java.util.*

// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")
    alias(libs.plugins.navigation)
}

kapt {
    // We also need to add `org.gradle.caching=true` in `gradle.properties`.
    useBuildCache = true
}

android {
    val appName = "LeoTemplate"

    defaultConfig {
        applicationId = namespace

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        multiDexEnabled = true

//        ndk {
//            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
//        }

        // buildConfigFieldFromGradleProperty("apiBaseUrl")
        // buildConfigField("FEATURE_MODULE_NAMES", getFeatureNames())
    }

    val releaseSigning = signingConfigs.create("release") {
        keyAlias = getSignProperty("keyAlias")
        keyPassword = getSignProperty("keyPassword")
        storeFile = File(rootDir, getSignProperty("storeFile"))
        storePassword = getSignProperty("storePassword")
        enableV1Signing = true
        enableV2Signing = true
        enableV3Signing = true
        enableV4Signing = true
    }

    buildTypes {
        getByName("debug") {
            signingConfig = releaseSigning
        }

        getByName("release") {
            signingConfig = releaseSigning
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

fun Project.getSignProperty(key: String, path: String = "conf/keystore.properties"): String {
    return Properties().apply { rootProject.file(path).inputStream().use(::load) }.getProperty(key)
}

dependencies {
    implementation(libs.android.material)
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.androidx.main)
    implementation(libs.bundles.navigation)
    implementation(libs.bundles.lifecycle)

    implementation(project(":module_common"))

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.bundles.test.runtime.only)
    androidTestImplementation(libs.bundles.android.test)
}
