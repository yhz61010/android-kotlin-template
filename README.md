[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.7.10-blue)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-7.3.0-orange)](https://developer.android.com/studio/releases/gradle-plugin)
[![Gradle](https://img.shields.io/badge/Gradle-7.5.1-green)](https://gradle.org)

A pure Android kotlin template project that let you create Android Kotlin project.
Using `Android + Kotlin + ktlint + Detekt + Gradle Kotlin DSL + Version Catalog(TOML)`

## Features
- 100% Kotlin-only template.
- 100% Gradle Kotlin DSL setup.
- Dependency versions managed via `Gradle Version Catalog`.
- Sample Espresso, Instrumentation & JUnit tests.
- Kotlin Static Analysis via detekt and ktlint.

## Gradle Setup
This template is using [**Gradle Kotlin DSL**](https://docs.gradle.org/current/userguide/kotlin_dsl.html) as well as the [Plugin DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block) to setup the build.

Dependencies are centralized inside the `Gradle Version Catalog` in the [libs.versions.toml](gradle/libs.versions.toml) file in the `gradle` folder.

## Static Analysis
This template is using [**detekt**](https://github.com/detekt/detekt) to analyze the source code, 
with the configuration that is stored in the [detekt.yml](conf/detekt.yml) file (the file has been generated with the `detektGenerateConfig` task). 
It also uses the **detekt-formatting** plugin which includes the ktlint rules (see https://detekt.dev/docs/rules/formatting/).

###  Runs the ktlint formatter on all kotlin sources in this project.
```kotlin
./gradlew ktlintFormat
```

### Displays the dependency updates for the project.
```kotlin
./gradlew dependencyUpdates
```

### Runs ktlint on all kotlin sources in this project.
```kotlin
./gradlew ktlintCheck
```

### Runs detekt
```kotlin
./gradlew detekt
```

### Check All
```kotlin
./gradlew staticCheck
```

### Gradle plugin for updating a project version catalog 
[version-catalog-update-plugin](https://github.com/littlerobots/version-catalog-update-plugin)

## How to enable and run jacoco
1. Add following line before `plugins` in your module-level `build.gradle.kts`:
```kotlin
apply(from = "../jacoco.gradle.kts")
```
2. Add `jacoco` plugin to the module which you want to check. Modify your module-level `build.gradle.kts` as following:
```kotlin
plugins {
    // Your other plugins.
    jacoco
}
```
3. After `Sync` project, you'll get two tasks `jacocoTestReport` and `jacocoCoverageVerification`.
Run them as you wish.

## Enable sonarqube
Add `sonarqube` plugin to the module which you want to check. Modify your module-level `build.gradle.kts` as following:
```kotlin
plugins {
    // Your other plugins.
    alias(libs.plugins.sonarqube)
}
```
