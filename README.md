# JVM Downgrader

IntelliJ IDEA plugin for Gradle projects that compile modern Java syntax through JVM Downgrader or Jabel while producing Java 8-compatible bytecode.

Target platform: IntelliJ IDEA 2026.1.2, build `261.24374.151`.

## What it changes

The plugin only updates the IntelliJ module source language level after a Gradle import, after changes to the relevant Gradle properties, or after applying its settings. It does not change Gradle compiler configuration, delegation, library roots, source roots, JAR files, PSI, navigation handlers, or dependency order entries.

As a result, Java library navigation, attached sources, code completion, Find Usages, and normal multi-release JAR resolution remain provided by IntelliJ IDEA. Use Gradle to build the project; IntelliJ's language-level presentation is not a build replacement.

## Detection

The plugin reads these files from each imported Gradle root:

- `gradle.properties`
- `gradle/gradle-daemon-jvm.properties`

Rules:

- `enableModernJavaSyntax=jvmDowngrader`: uses the highest number in `jvmDowngraderMultiReleaseVersions`; when absent, uses `forceToolchainVersion`, then `toolchainVersion` from the daemon properties.
- `enableModernJavaSyntax=jabel`: uses `forceToolchainVersion`, then daemon `toolchainVersion`. `downgradeTargetVersion` is intentionally ignored because it is a bytecode target, not the Java source syntax level.
- Other modes are untouched in automatic mode.

The embedded JAR scanner recognizes JVM Downgrader artifacts only when their class data contains the `xyz/wagyourtail/jvmdg/` marker. It records multi-release layers without reclassifying ordinary multi-release JARs, so normal IntelliJ handling remains authoritative for all dependencies.

For example, an artifact with Java 8 base classes and `META-INF/versions/17`, `21`, and `25` layers is recognized as JVM Downgrader metadata, while a project configured with `jvmDowngraderMultiReleaseVersions=17,21,25` receives Java 25 as its source language level rather than Java 8.

## Settings

Settings are under **Settings | Tools | JVM Downgrader**.

- Global default: `Automatic` or a fixed Java 8/11/17/21/25 level.
- Project default: `Inherit global setting`, `Automatic`, or a fixed level.
- Project `Inherit global setting` uses the global selection. Any other project value overrides it.

When the selected level has no configured Java SDK at least that new, the module is left unchanged and a diagnostic is written to the IDE log. Add a suitable SDK in IntelliJ IDEA, then refresh the project.

## Build

Use JDK 25 for CI and local release builds:

```powershell
.\gradlew.bat --no-daemon clean test verifyPlugin buildPlugin
```

The plugin ZIP is written to `build/distributions/`. GitHub Actions runs the same verification on `master` and uploads the ZIP; tags starting with `v` also attach it to a GitHub release.
