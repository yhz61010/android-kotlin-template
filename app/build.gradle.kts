import java.io.ByteArrayOutputStream
import java.util.*

// https://developer.android.com/studio/build?hl=zh-cn#module-level

apply(from = "../jacoco.gradle.kts")
// https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.parcelize) // id("kotlin-parcelize")

    // https://github.com/mannodermaus/android-junit5
    alias(libs.plugins.android.junit5)

    alias(libs.plugins.sonarqube)
    jacoco
}

kapt {
    // We also need to add `org.gradle.caching=true` in `gradle.properties`.
    useBuildCache = true
}

android {
    val appName = "LeoTemplate"

    /** The app's namespace. Used primarily to access app resources. */
    namespace = "com.leovp.androidtemplate"

    resourcePrefix = "app_"

    /** Specifies one flavor dimension. */
    flavorDimensions += listOf("version")

    defaultConfig {
        applicationId = namespace

        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        multiDexEnabled = true

        ndk {
            // abiFilters "arm64-v8a", "armeabi-v7a", "x86", "x86_64"
            @android.annotation.SuppressLint("ChromeOsAbiSupport")
            abiFilters += setOf("arm64-v8a")
        }

        // buildConfigFieldFromGradleProperty("apiBaseUrl")
        // buildConfigField("FEATURE_MODULE_NAMES", getFeatureNames())

        // https://github.com/mannodermaus/android-junit5
        // Connect JUnit 5 to the runner
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }

    // https://medium.com/androiddevelopers/5-ways-to-prepare-your-app-build-for-android-studio-flamingo-release-da34616bb946
    buildFeatures {
        // dataBinding = true
        // aidl = true

        // viewBinding is enabled by default. Check [build.gradle.kts] in the root folder of project.
        // viewBinding = true

        // Enable compose feature
        compose = true

        // Generate BuildConfig.java file
        buildConfig = true
    }

    // Compose options setting
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    signingConfigs {
        named("debug") {
            keyAlias = getSignProperty("keyAlias")
            keyPassword = getSignProperty("keyPassword")
            storeFile = File(rootDir, getSignProperty("storeFile"))
            storePassword = getSignProperty("storePassword")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }

        create("release") {
            keyAlias = getSignProperty("keyAlias")
            keyPassword = getSignProperty("keyPassword")
            storeFile = File(rootDir, getSignProperty("storeFile"))
            storePassword = getSignProperty("storePassword")
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    /**
     * The productFlavors block is where you can configure multiple product flavors.
     * This allows you to create different versions of your app that can
     * override the defaultConfig block with their own settings. Product flavors
     * are optional, and the build system does not create them by default.
     *
     * This example creates a free and paid product flavor. Each product flavor
     * then specifies its own application ID, so that they can exist on the Google
     * Play Store, or an Android device, simultaneously.
     *
     * If you declare product flavors, you must also declare flavor dimensions
     * and assign each flavor to a flavor dimension.
     */
    productFlavors {
        create("demo") {
            // Assigns this product flavor to the "version" flavor dimension.
            // If you are using only one dimension, this property is optional,
            // and the plugin automatically assigns all the module's flavors to
            // that dimension.
            dimension = "version"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
        }
        create("full") {
            dimension = "version"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }

        /**
         * By default, Android Studio configures the release build type to enable code
         * shrinking, using minifyEnabled, and specifies the default Proguard rules file.
         *
         * See the global configurations in top-level `build.gradle.kts`.
         */
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }

        /**
         * The `initWith` property allows you to copy configurations from other build types,
         * then configure only the settings you want to change. This one copies the debug build
         * type, and then changes the manifest placeholder and application ID.
         */
        // Uncommented the following block will cause build exception
        // if using ./gradlew build command
        // create("staging") {
        //     initWith(getByName("debug"))
        //     // https://developer.android.com/studio/build/manifest-build-variables
        //     // manifestPlaceholders["hostName"] = "internal.example.com"
        //     // applicationIdSuffix = ".staging"
        // }
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

fun Project.getSignProperty(key: String, path: String = "config/sign/keystore.properties"): String {
    return Properties().apply { rootProject.file(path).inputStream().use(::load) }.getProperty(key)
}

dependencies {
    // By using `projects`, you need to enable `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`
    // in `settings.gradle.kts` where in your root folder.
    implementation(projects.featureCommon)

    implementation(libs.coil.kt.compose)

    // ==============================
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.bundles.test.runtime.only)
    androidTestImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.android.test)
    // ==============================
    // The instrumentation test companion libraries
    // https://github.com/mannodermaus/android-junit5
    // ==============================
    androidTestImplementation(libs.mannodermaus.junit5.core)
    androidTestRuntimeOnly(libs.mannodermaus.junit5.runner)
    // ==============================
}
