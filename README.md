# JVM Downgrader

IntelliJ IDEA plugin that parses JVM Downgrader and Jabel dependency source attachments with the Java language level they require.

Target platform: IntelliJ IDEA 2026.1.2, build `261.24374.151`.

## What it changes

The plugin only provides a file-level language level for Java files in dependency source attachments. Project files always keep the language level configured in **Project Structure**. It does not change Gradle compiler configuration, delegation, module language levels, SDKs, library roots, source roots, JAR files, PSI ownership, navigation handlers, or dependency order entries.

Java library navigation, attached sources, code completion, Find Usages, and normal multi-release JAR resolution remain provided by IntelliJ IDEA. Use Gradle to build the project; this plugin is not a build replacement.

## Detection

The plugin obtains the library owning the opened source attachment through IntelliJ's `LibraryOrderEntry`; it never reads the library's or consuming project's Gradle configuration.

- A JVM Downgrader artifact is recognized only when its class data contains the generic `xyz/wagyourtail/jvmdg/` marker. Its level is the largest valid `META-INF/versions/<N>` layer in that class JAR.
- Jabel outputs compatibility bytecode and does not retain a source-language marker. For such source attachments, the plugin determines the minimum language level from actual Java syntax, including `var`, pattern `instanceof`, enhanced `switch`, pattern `switch`, records, sealed types, and text blocks.
- In automatic mode, ordinary Java 8 dependencies return no override and IntelliJ IDEA keeps its native behavior.

No dependency coordinate, artifact name, package name, or hardcoded project is used for detection.

## Settings

Settings are under **Settings | Tools | JVM Downgrader**.

- Global default: `Automatic` or a fixed Java 8/11/17/21/25 library source level.
- Project default: `Inherit global setting`, `Automatic`, or a fixed library source level.
- Project `Inherit global setting` uses the global selection. Any other project value overrides it.

Fixed settings are used only while parsing dependency source attachments. They never update a module's language level.

## Build

Use JDK 25 for CI and local release builds:

```powershell
.\gradlew.bat --no-daemon clean test verifyPlugin buildPlugin
```

The plugin ZIP is written to `build/distributions/`. GitHub Actions runs the same verification on `master` and uploads the ZIP; tags starting with `v` also attach it to a GitHub release.
