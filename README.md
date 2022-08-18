# android-template
This is an Android kotlin template project.

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

## How to enable and run jacoco
1. Add following line before `plugins` in your module-level `build.gradle.kts`:
```kotlin
apply(from = "../jacoco.gradle.kts")
```
2. Add `jacoco` in your module-level `build.gradle.kts` as following:
```kotlin
plugins {
    // Your other plugins.
    jacoco
}
```
3. After `Sync` project, you'll get two tasks `jacocoTestReport` and `jacocoCoverageVerification`.
Run them as you wish.
