import com.android.build.gradle.BaseExtension
import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
//    id(GradlePluginId.ANDROID_APPLICATION) apply false
//    id(GradlePluginId.ANDROID_LIBRARY) apply false
//    id(GradlePluginId.KOTLIN_ANDROID) apply false
//    id(GradlePluginId.SAFE_ARGS) apply false

//    id 'com.android.application' version '7.2.2' apply false
//    id 'com.android.library' version '7.2.2' apply false
//    id 'org.jetbrains.kotlin.android' version '1.7.10' apply false

//    id(GradlePluginId.ANDROID_JUNIT_5) apply false

    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

//tasks {
//    register("clean", Delete::class) {
//        delete(rootProject.buildDir)
//    }
//}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

// all projects = root project + sub projects
allprojects {
    group = PUBLISHING_GROUP

    // We want to apply ktlint at all project level because it also checks Gradle config files (*.kts)
    apply(plugin = GradlePluginId.KTLINT_GRADLE)

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
    tasks.withType<Test> {
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
    }

    apply(plugin = GradlePluginId.DETEKT)
    // or
    // apply {
    //     plugin(GradlePluginId.DETEKT)
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

fun Project.configureAndroid() {
    (project.extensions.findByName("android") as? BaseExtension)?.run {
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
