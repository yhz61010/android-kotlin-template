import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.dsl.BaseFlavor
import com.android.build.gradle.internal.dsl.DefaultConfig
import io.gitlab.arturbosch.detekt.Detekt

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
    group = "com.leovp"

    // We want to apply ktlint at all project level because it also checks Gradle config files (*.kts)
    apply(plugin = rootProject.libs.plugins.ktlint.get().pluginId)

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }

    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
        useJUnitPlatform()
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
}

subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)
    // or
    // apply {
    //     plugin(rootProject.libs.plugins.detekt.get().pluginId)
    // }

    detekt {
        config = files("$rootDir/detekt.yml")

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

    afterEvaluate {
        configureAndroid()
    }
}

//tasks {
//    register("clean", Delete::class) {
//        delete(rootProject.buildDir)
//    }
//}

//tasks.register<Delete>("clean") {
//    delete(rootProject.buildDir)
//}

fun Project.configureAndroid() {
    (project.extensions.getByName<BaseExtension>("android")).apply {
        sourceSets {
            map { it.java.srcDir("src/${it.name}/kotlin") }
        }
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
Mimics all static checks that run on CI.
Note that this task is intended to run locally (not on CI), because on CI we prefer to have parallel execution
and separate reports for each of the checks (multiple statuses eg. on github PR page).
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
