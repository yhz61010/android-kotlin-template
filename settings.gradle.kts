rootProject.name = "android_kotlin_template"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    resolutionStrategy {
        eachPlugin {
//            println("id=${requested.id.id} version=${requested.version} namespace=${requested.id.namespace}")
//            when (requested.id.id) {
//                "com.android.application",
//                "com.android.library",
//                "com.android.dynamic-feature" -> {
//                    val agpCoordinates: String by settings
//                    useModule(agpCoordinates)
//                }
//                "androidx.navigation.safeargs.kotlin" -> {
//                    val navigationCoordinates: String by settings
//                    useModule(navigationCoordinates)
//                }
//                "de.mannodermaus.android-junit5" -> {
//                    val androidJnit5Coordinates: String by settings
//                    useModule(androidJnit5Coordinates) // navigationVersion
//                }
//            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral {
            isAllowInsecureProtocol = true
        }
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
}

// https://docs.gradle.org/7.0/release-notes.html
// Type-safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app",
    "module_common"
)
