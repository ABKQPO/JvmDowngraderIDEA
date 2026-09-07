package com.hfstudio.jvmdowngraderidea.detection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradleModernJavaDetectorTest {
    @Test
    fun `jvm downgrader selects the highest multi release version`() {
        val result = GradleModernJavaDetector.detect(
            """
            enableModernJavaSyntax = jvmDowngrader
            downgradeTargetVersion = 8
            jvmDowngraderMultiReleaseVersions = 17, 21, 25
            """.trimIndent(),
            "toolchainVersion=25"
        )

        assertEquals(ModernJavaMode.JVM_DOWNGRADER, result.mode)
        assertEquals(25, result.detectedSourceVersion)
        assertEquals(8, result.downgradeTargetVersion)
        assertEquals(setOf(17, 21, 25), result.multiReleaseVersions)
    }

    @Test
    fun `jabel uses the toolchain and ignores downgrade target`() {
        val result = GradleModernJavaDetector.detect(
            "enableModernJavaSyntax=jabel\ndowngradeTargetVersion=8",
            "toolchainVersion=21"
        )

        assertEquals(ModernJavaMode.JABEL, result.mode)
        assertEquals(21, result.detectedSourceVersion)
        assertNull(result.downgradeTargetVersion)
    }

    @Test
    fun `force toolchain overrides daemon toolchain`() {
        val result = GradleModernJavaDetector.detect(
            "enableModernJavaSyntax=jabel\nforceToolchainVersion=25",
            "toolchainVersion=21"
        )

        assertEquals(25, result.detectedSourceVersion)
    }

    @Test
    fun `unmanaged modes have no detected source version`() {
        val result = GradleModernJavaDetector.detect("enableModernJavaSyntax=modern", "toolchainVersion=25")

        assertEquals(ModernJavaMode.NONE, result.mode)
        assertNull(result.detectedSourceVersion)
    }
}
