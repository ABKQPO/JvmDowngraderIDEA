package com.hfstudio.jvmdowngraderidea.detection

import java.nio.file.Files
import java.nio.file.Path

object GradleProjectModernJavaDetector {
    fun detect(gradleRoot: Path): ModernJavaDetection = GradleModernJavaDetector.detect(
        gradleProperties = readIfRegularFile(gradleRoot.resolve("gradle.properties")),
        daemonProperties = readIfRegularFile(gradleRoot.resolve("gradle/gradle-daemon-jvm.properties")),
    )

    private fun readIfRegularFile(path: Path): String = runCatching {
        if (Files.isRegularFile(path)) Files.readString(path) else ""
    }.getOrDefault("")
}
