import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import io.gitlab.arturbosch.detekt.Detekt
import java.util.Locale
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintExtension

// =====================================
// ========== Global settings ==========
// =====================================

val customGroup = "com.leovp"

/**
 * You can use it in subproject like this:
 * ```kotlin
 * val jdkVersion: JavaVersion by rootProject.extra
 * ```
 */
val jdkVersion: JavaVersion by extra { JavaVersion.VERSION_17 }

/**
 * By default, the resource prefix is just the module name.
 *
 * resourcePrefix 的校验规则：
 * 1. 针对可识别类型的文件夹中的非 `values` 文件夹目录，校验 `xml` 文件的文件前缀是否符合规则。
 * 2. 针对 `values` 文件夹中的文件，不校验文件前缀，校验文件中 name 元素的值。
 * 3. 针对二进制文件，校验文件前缀。
 * 4. 图片资源通常来说也应该被检验文件前缀，但是我们通常会进行如下 lint 设置 abortOnError = false 因此错误提示被忽略了。
 *
 * **Attention:**
 * All the occurrences `-` dash in `Module Name` will be replaced with `_` underscore.
 *
 * @see <a href="https://blog.csdn.net/weixin_43910395/article/details/120166450">resourcePrefix</a>
 */
// private val useResourcePrefix = true

// =====================================

// https://developer.android.com/studio/build?hl=zh-cn#top-level

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    /**
     * You should use `apply false` in the top-level build.gradle file
     * to add a Gradle plugin as a build dependency, but not apply it to the
     * current (root) project. You should not use `apply false` in sub-projects.
     * For more information, see
     * Applying external plugins with same version to subprojects.
     */

    // https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint.gradle)
    alias(libs.plugins.benmanes.versions)
    // alias(libs.plugins.vcu)
    jacoco

    // id("org.jetbrains.kotlin.kapt") // or kotlin("kapt")
    // If you use kotlin(), you can change dash(-) with dot(.)
    // or you can still use dash like id("kotlin-parcelize")
    // alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false // id("kotlin-parcelize")
    // https://stackoverflow.com/a/72508037/1685062
    alias(libs.plugins.navigation) apply false
}

// ********************
// https://gist.github.com/huuphuoc1396/497c27c1fac205f5d9c6696016227e1c
//
// Fix the problem: "Unexpected SMAP line: *S KotlinDebug"
// ********************
jacoco {
    toolVersion = rootProject.libs.versions.jacoco.get()
}

val detektFormatting: Provider<MinimalExternalModuleDependency> = libs.detekt.formatting

// all projects = root project + sub projects
allprojects {
    group = customGroup

    // We want to apply ktlint at all project level because it also checks Gradle config files (*.kts)
    apply(plugin = rootProject.libs.plugins.ktlint.gradle.get().pluginId)
    configure<KtlintExtension> {
        version.set(rootProject.libs.versions.ktlint.asProvider().get())
    }

    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    // or
    // apply {
    //     plugin(rootProject.libs.plugins.detekt.get().pluginId)
    // }

    detekt {
        config.from(files("$rootDir/config/detekt/detekt.yml"))

        parallel = true

        // By default detekt does not check test source set and gradle specific files,
        // so hey have to be added manually.
        source.from(
            files(
                "$rootDir/buildSrc",
                "$rootDir/build.gradle.kts",
                "$rootDir/settings.gradle.kts",
                "src/main/kotlin",
                "src/test/kotlin"
            )
        )
    }

    // Ktlint configuration for sub-projects
    ktlint {
        verbose.set(true)
        android.set(true)

        // Uncomment below line and run .\gradlew ktlintCheck to see check ktlint experimental rules
        // enableExperimentalRules.set(true)

        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }

        filter {
            exclude { element -> element.file.path.contains("generated/") }
        }
    }

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        useJUnitPlatform()
    }

    afterEvaluate {
        configureCompileVersion()
    }

    // configurations.all {
    //     resolutionStrategy.eachDependency {
    //         println(
    //             "module=${requested.module}:${requested.version} group=${requested.group} name=${requested.name}"
    //         )
    //     }
    // }

    dependencies {
        // We must define `detektFormatting` in outside, or else the compile error will occur.
        detektPlugins(detektFormatting)
    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.kotlin.android.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kotlin.kapt.get().pluginId)

    plugins.withId(rootProject.libs.plugins.android.application.get().pluginId) {
        // println("displayName=$displayName, name=$name, group=$group")
        configureApplication()
    }

    plugins.withId(rootProject.libs.plugins.android.library.get().pluginId) { configureLibrary() }
}

// tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
// }

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

/**
 * Configure to eliminate the following warning:
 * ```
 * 'compileReleaseJavaWithJavac' task (current target is 1.8) and 'compileReleaseKotlin'
 * task (current target is 11) jvm target compatibility should be set to the same Java version.
 * ```
 */
fun Project.configureCompileVersion() {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = jdkVersion.toString()
        targetCompatibility = jdkVersion.toString()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = jdkVersion.toString()
        }
    }
}

fun Project.configureBase(): BaseExtension {
    return extensions.getByName<BaseExtension>("android").apply {
        // You need to _resourcePrefix_ on your each module.
        // if (useResourcePrefix) {
        //     if (resourcePrefix == null) {
        //         val moduleName = name.replace("-", "_")
        //         resourcePrefix = "${moduleName}_"
        //     }
        // }
        compileSdkVersion(rootProject.libs.versions.compile.sdk.get().toInt())
        defaultConfig {
            minSdk = rootProject.libs.versions.min.sdk.get().toInt()
            targetSdk = rootProject.libs.versions.target.sdk.get().toInt()

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        sourceSets.configureEach {
            // This `name` is just the name for each `source` in `sourceSets`.
            java.srcDirs("src/$name/kotlin", "src/$name/java")
        }
        // sourceSets {
        //     map { it.java.srcDir("src/${it.name}/kotlin") }
        // }
        testOptions {
            unitTests {
                isReturnDefaultValues = true
                isIncludeAndroidResources = true
            }
        }
        compileOptions {
            // setDefaultJavaVersion(jdkVersion)
            sourceCompatibility = jdkVersion
            targetCompatibility = jdkVersion
        }
        buildTypes {
            getByName("release") {
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }

        buildFeatures.viewBinding = true
        // https://medium.com/androiddevelopers/5-ways-to-prepare-your-app-build-for-android-studio-flamingo-release-da34616bb946
        // Add this line as needed
        // buildFeatures.buildConfig = true

        // Turn off checking the given issue id's
        lintOptions.disable += setOf(
            // "MissingTranslation",
            // "GoogleAppIndexingWarning",
            "RtlHardcoded",
            "RtlCompat",
            "RtlEnabled"
        )
        packagingOptions.resources.pickFirsts += setOf(
            // kotlinx-coroutines-android
            "META-INF/atomicfu.kotlin_module"
        )
        packagingOptions.resources.excludes += setOf(
            "META-INF/licenses/**",
            "META-INF/NOTICE*",
            "META-INF/LICENSE*",
            "META-INF/DEPENDENCIES*",
            "META-INF/INDEX.LIST",
            "META-INF/io.netty.versions.properties",
            "META-INF/services/reactor.blockhound.integration.BlockHoundIntegration",

            // Multiple dependency bring these files in. Exclude them to enable
            // our test APK to build (has no effect on our AARs)
            "META-INF/{AL2.0,LGPL2.1}",

            "**/*.proto",
            "**/*.bin",
            "**/*.java"
            // "**/*.properties",
            // "**/*.version",
            // ==============================
            // ==============================
            // Don't exclude [kotlin_module] file.
            // Or else, you can't import kotlin extension methods in kotlin file.
            // "**/*.*_module", // **/*.kotlin_module
            // ==============================
            // ==============================
            // "*.txt",
            // "kotlin/**",
            // "kotlinx/**",
            // "okhttp3/**",
            // "META-INF/services/**",
        )
    }
}

/**
 * The application level default configurations.
 * You just need to add your custom properties as you wish.
 *
 * **Attention**:
 * The default value of `applicationId` is `namespace`.
 */
fun Project.configureApplication(): BaseExtension = configureBase().apply {
    defaultConfig {
        applicationId = namespace
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
        }

        getByName("debug") {
            isShrinkResources = false
            isMinifyEnabled = false
        }
    }
}

/**
 * All the submodules will have the hierarchy configurations.
 * You just need to add your custom properties as you wish.
 */
fun Project.configureLibrary(): BaseExtension = configureBase().apply {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }

        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}

// Target version of the generated JVM bytecode. It is used for type resolution.
tasks.withType<Detekt>().configureEach {
    jvmTarget = jdkVersion.toString()

    reports {
        // observe findings in your browser with structure and code snippets
        html.required.set(true)
        // checkstyle like format mainly for integrations like Jenkins
        xml.required.set(true)
        // similar to the console output, contains issue signature to manually edit baseline files
        txt.required.set(true)
        // standardized SARIF format (https://sarifweb.azurewebsites.net/)
        // to support integrations with Github Code Scanning
        sarif.required.set(true)
        // simple Markdown format
        md.required.set(true)
    }
}

/*
 * Mimics all static checks that run on CI.
 * Note that this task is intended to run locally (not on CI), because on CI we prefer to have parallel execution
 * and separate reports for each of the checks (multiple statuses eg. on github PR page).
 */
task("staticCheck") {
    group = "verification"

    afterEvaluate {
        // Filter modules with "lintDebug" task (non-Android modules do not have lintDebug task)
        val lintTasks = subprojects.mapNotNull { "${it.name}:lintDebug" }

        // Get modules with "testDebugUnitTest" task (app module does not have it)
        val testTasks =
            subprojects.mapNotNull { "${it.name}:testDebugUnitTest" }.filter { it != "app:testDebugUnitTest" }

        // All task dependencies
        val taskDependencies = mutableListOf("app:assembleAndroidTest", "ktlintCheck", "detekt").also {
            it.addAll(lintTasks)
            it.addAll(testTasks)
        }

        // By defining Gradle dependency all dependent tasks will run before this "empty" task
        dependsOn(taskDependencies)
    }
}

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase(Locale.getDefault()).contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

// --------------------------------------

/** Takes value from Gradle project property and sets it as build config property. */
fun BaseFlavor.buildConfigFieldFromGradleProperty(gradlePropertyName: String) {
    val propertyValue = project.properties[gradlePropertyName] as? String
    checkNotNull(propertyValue) { "Gradle property $gradlePropertyName is null" }

    val androidResourceName = "GRADLE_${gradlePropertyName.toSnakeCase()}".uppercase(Locale.getDefault())
    buildConfigField("String", androidResourceName, propertyValue)
}

fun String.toSnakeCase() = this.split(Regex("(?=[A-Z])")).joinToString("_") { it.lowercase(Locale.getDefault()) }

/* Adds a new field to the generated BuildConfig class. */
fun DefaultConfig.buildConfigField(name: String, value: Array<String>) {
    // Create String that holds Java String Array code
    val strValue = value.joinToString(prefix = "{", separator = ",", postfix = "}", transform = { "\"$it\"" })
    buildConfigField("String[]", name, strValue)
}
