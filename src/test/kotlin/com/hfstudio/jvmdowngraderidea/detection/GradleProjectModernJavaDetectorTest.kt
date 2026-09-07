package com.hfstudio.jvmdowngraderidea.detection

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Test

class GradleProjectModernJavaDetectorTest {
    @Test
    fun `reads jabel toolchain from the Gradle daemon configuration`() {
        val root = Files.createTempDirectory("jabel-project")
        Files.writeString(root.resolve("gradle.properties"), "enableModernJavaSyntax=jabel")
        Files.createDirectories(root.resolve("gradle"))
        Files.writeString(root.resolve("gradle/gradle-daemon-jvm.properties"), "toolchainVersion=21")

        val result = GradleProjectModernJavaDetector.detect(root)

        assertEquals(ModernJavaMode.JABEL, result.mode)
        assertEquals(21, result.detectedSourceVersion)
    }
}
