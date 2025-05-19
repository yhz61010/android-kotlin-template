[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.1.21-blue)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-8.10.0-orange)](https://developer.android.com/studio/releases/gradle-plugin)
[![Gradle](https://img.shields.io/badge/Gradle-8.14-green)](https://gradle.org)

[![Android Studio](https://img.shields.io/badge/Android_Studio-Meerkat_Feature_Drop_|_2024.3.2-green)](https://developer.android.com/studio)
[![Build Java Version](https://img.shields.io/badge/JDK-17.0.6-green)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Compatibility Java Version](https://img.shields.io/badge/Compatibility_Java_-17-green)](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
[![NDK](https://img.shields.io/badge/NDK-25.2.9519653-green)](https://developer.android.com/ndk/downloads)

A pure Android kotlin template project that let you create Android Kotlin project.

Using `Android + Kotlin + ktlint + Detekt + Gradle Kotlin DSL + Version Catalog(TOML)`

## Attention
Starting with Android 14, apps with a targetSdkVersion lower than 23 can't be installed.
So the min sdk of this project is 23 too.

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
with the configuration that is stored in the [detekt.yml](config/detekt/detekt.yml) file (the file has been generated with the `detektGenerateConfig` task). 
It also uses the **detekt-formatting** plugin which includes the ktlint rules (see https://detekt.dev/docs/rules/formatting/).

###  Runs the ktlint formatter on all kotlin sources in this project.
```shell
./gradlew ktlintFormat
```

### Displays the dependency updates for the project.
```shell
./gradlew dependencyUpdates
```

### Runs ktlint on all kotlin sources in this project.
```shell
./gradlew ktlintCheck
```

### Runs detekt
```shell
./gradlew detekt
```

### Check All
```shell
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
