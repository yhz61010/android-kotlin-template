import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig
import io.gitlab.arturbosch.detekt.Detekt

// =====================================
// ========== Global settings ==========
// =====================================

/**
 * **Attention:**
 * The rule for each package name of module is as following:
 * ```
 * customGroup + "." + moduleName
 * ```
 * For example:
 * ```
 * com.leovp.module_one
 * ```
 * **Attention:** All the occurrences `-` dash in `moduleName` will be replaced with `_` underscore.
 */
val customGroup = "com.leovp"

/**
 * All the occurrences `-` dash in `appPkg` will be replaced with `_` underscore.
 *
 * **Attention:**
 * The rule for `sourceSets` is as following.
 * For example:
 * ```
 * sourceSets.configureEach {
 *      java.srcDirs("src/$moduleName/kotlin")
 * }
 * ```
 *
 * **Attention:** All the occurrences `-` dash in `moduleName` will be replaced with `_` underscore.
 */
val appPkg = "com.leovp.androidtemplate"

/**
 * By default, the resource prefix is just the module name.
 *
 * resourcePrefix 的校验规则：
 * 1. 针对可识别类型的文件夹中的非 `values` 文件夹目录，校验 `xml` 文件的文件前缀是否符合规则。
 * 2. 针对 `values` 文件夹中的文件，不校验文件前缀，校验文件中name元素的值。
 * 3. 针对二进制文件，校验文件前缀。
 * 4. 图片资源通常来说也应该被检验文件前缀，但是我们通常会进行如下 lint 设置 abortOnError = false
 * 因此错误提示被忽略了。
 *
 * @see [resourcePrefix](https://blog.csdn.net/weixin_43910395/article/details/120166450)
 */
val useResourcePrefix = true

// =====================================

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // https://docs.gradle.org/current/userguide/plugins.html#sec:subprojects_plugins_dsl
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    alias(libs.plugins.kotlin.kapt) apply false // id("org.jetbrains.kotlin.kapt") or kotlin("kapt")
    alias(libs.plugins.kotlin.parcelize) apply false // id("kotlin-parcelize")

    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

// all projects = root project + sub projects
allprojects {
    // The group name is also the prefix of application package name as well as the prefix of submodules package name.
    group = customGroup

    // We want to apply ktlint at all project level because it also checks Gradle config files (*.kts)
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    // or
    // apply {
    //     plugin(rootProject.libs.plugins.detekt.get().pluginId)
    // }

    detekt {
        config = files("$rootDir/conf/detekt.yml")

        parallel = true

        // By default detekt does not check test source set and gradle specific files, so hey have to be added manually
        source = files(
            "$rootDir/buildSrc",
            "$rootDir/build.gradle.kts",
            "$rootDir/settings.gradle.kts",
            "src/main/kotlin",
            "src/test/kotlin"
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

//    configurations.all {
//        resolutionStrategy.eachDependency {
//            println("requested.group=${requested.group} requested.name=${requested.name} " +
//                    "requested.module=${requested.module} requested.version=${requested.version}")
//        }
//    }
}

subprojects {
    apply(plugin = rootProject.libs.plugins.kotlin.android.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kotlin.kapt.get().pluginId)

    plugins.withId(rootProject.libs.plugins.android.application.get().pluginId) {
        // println("displayName=$displayName, name=$name, group=$group")
        // You can use `group` which is the value that is set in `allprojects`.
        // The `ns` parameter just means the application namespace aka app package name.
        configureApplication(appPkg)
    }

    plugins.withId(rootProject.libs.plugins.android.library.get().pluginId) { configureLibrary() }
}

//tasks {
//    register("clean", Delete::class) {
//        delete(rootProject.buildDir)
//    }
//}

//tasks.register<Delete>("clean") {
//    delete(rootProject.buildDir)
//}

fun Project.configureCompileVersion() {
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
}

fun Project.configureBase(): BaseExtension {
    return extensions.getByName<BaseExtension>("android").apply {
        val moduleName = name.replace('-', '_')
        if (useResourcePrefix) {
            resourcePrefix = "${moduleName}_"
        }
        compileSdkVersion(rootProject.libs.versions.compile.sdk.get().toInt())
        defaultConfig {
            minSdk = rootProject.libs.versions.min.sdk.get().toInt()
            targetSdk = rootProject.libs.versions.target.sdk.get().toInt()

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        sourceSets.configureEach {
            java.srcDirs("src/$moduleName/kotlin")
        }
//        sourceSets {
//            map { it.java.srcDir("src/${it.moduleName}/kotlin") }
//        }
        compileOptions.setDefaultJavaVersion(JavaVersion.VERSION_11)
        buildTypes {
            getByName("release") {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }

            getByName("debug") {
                isMinifyEnabled = false
            }
        }
        buildFeatures.viewBinding = true
        // turn off checking the given issue id's
        lintOptions.disable += setOf(
            // "MissingTranslation",
            // "GoogleAppIndexingWarning",
            "RtlHardcoded",
            "RtlCompat",
            "RtlEnabled"
        )
        packagingOptions.resources.excludes += setOf(
            "META-INF/licenses/**",
            "META-INF/atomicfu.kotlin_module",
            "META-INF/NOTICE*",
            "META-INF/NOTICE*.*",
            "META-INF/DEPENDENCIES*",
            "META-INF/DEPENDENCIES*.*",
            "META-INF/LICENSE*",
            "META-INF/LICENSE*.*",
            "META-INF/INDEX.LIST",
            "META-INF/io.netty.versions.properties",
            "META-INF/{AL2.0,LGPL2.1}",
            "**/*.proto",
            "**/*.bin",
            "**/*.java",
            "**/*.properties",
            "**/*.version",
            "**/*.*_module",
            "*.txt",
            "kotlin/**",
            "kotlinx/**",
            "okhttp3/**",
            "META-INF/services/**",
        )
    }
}

/**
 * The application level default configurations.
 * You just need to add your custom properties as you wish.
 *
 * **Attention**:
 * All the occurrences `-` dash will be replaced with `_` underscore.
 *
 * @param ns The application namespace aka app package name.
 */
fun Project.configureApplication(ns: String): BaseExtension = configureBase().apply {
    namespace = ns.replace('-', '_')
    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        getByName("release") {
            isShrinkResources = true
        }

        getByName("debug") {
            isShrinkResources = false
        }
    }
}

/**
 * All the submodules will have the hierarchy configurations.
 * You just need to add your custom properties as you wish.
 *
 * **Attention**:
 * All the occurrences `-` dash will be replaced with `_` underscore.
 */
fun Project.configureLibrary(): BaseExtension = configureBase().apply {
    // The `group` is the value that is set in `allprojects`.
    namespace = "$group.${name.replace('-', '_')}"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

// Target version of the generated JVM bytecode. It is used for type resolution.
tasks.withType<Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_11.toString()

    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
        md.required.set(true) // simple Markdown format
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
        val testTasks = subprojects.mapNotNull { "${it.name}:testDebugUnitTest" }
            .filter { it != "app:testDebugUnitTest" }

        // All task dependencies
        val taskDependencies =
            mutableListOf("app:assembleAndroidTest", "ktlintCheck", "detekt").also {
                it.addAll(lintTasks)
                it.addAll(testTasks)
            }

        // By defining Gradle dependency all dependent tasks will run before this "empty" task
        dependsOn(taskDependencies)
    }
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

/* Adds a new field to the generated BuildConfig class */
fun DefaultConfig.buildConfigField(name: String, value: Array<String>) {
// Create String that holds Java String Array code
    val strValue = value.joinToString(prefix = "{", separator = ",", postfix = "}", transform = { "\"$it\"" })
    buildConfigField("String[]", name, strValue)
}
