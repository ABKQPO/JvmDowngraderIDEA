package com.hfstudio.jvmdowngraderidea.jar

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class JvmDowngraderJarScannerTest {
    @Test
    fun `scanner recognizes multi release entries and jvmdg marker`() {
        val jar = fixtureJar(
            "META-INF/versions/17/example/Api.class" to byteArrayOf(),
            "META-INF/versions/25/example/Api.class" to byteArrayOf(),
            "example/Marker.class" to "xyz/wagyourtail/jvmdg/j16/stub".encodeToByteArray()
        )

        assertEquals(
            JvmDowngraderJarInfo(isJvmDowngrader = true, multiReleaseVersions = setOf(17, 25)),
            JvmDowngraderJarScanner.scan(jar)
        )
    }

    @Test
    fun `ordinary jar is not reclassified`() {
        assertEquals(JvmDowngraderJarInfo(false, emptySet()), JvmDowngraderJarScanner.scan(fixtureJar()))
    }

    @Test
    fun `ordinary multi release jar is not classified as JVM Downgrader`() {
        assertEquals(
            JvmDowngraderJarInfo(false, setOf(21)),
            JvmDowngraderJarScanner.scan(fixtureJar("META-INF/versions/21/example/Api.class" to byteArrayOf()))
        )
    }

    @Test
    fun `marker in a resource file does not classify an archive as JVM Downgrader`() {
        val info = JvmDowngraderJarScanner.scan(
            fixtureJar(
                "META-INF/versions/21/example/Api.class" to byteArrayOf(),
                "META-INF/notice.txt" to "xyz/wagyourtail/jvmdg/".encodeToByteArray(),
            ),
        )

        assertFalse(info.isJvmDowngrader)
    }

    @Test
    fun `jvm downgrader archive exposes its highest multi release level`() {
        val jar = fixtureJar(
            "META-INF/versions/17/example/Api.class" to byteArrayOf(),
            "META-INF/versions/25/example/Api.class" to byteArrayOf(),
            "example/Marker.class" to "xyz/wagyourtail/jvmdg/j16/stub".encodeToByteArray(),
        )

        assertEquals(25, JvmDowngraderJarScanner.scan(jar).automaticLanguageLevel)
    }

    @Test
    fun `ordinary multi release archive has no automatic override`() {
        assertNull(
            JvmDowngraderJarScanner.scan(
                fixtureJar("META-INF/versions/25/example/Api.class" to byteArrayOf()),
            ).automaticLanguageLevel,
        )
    }

    @Test
    fun `corrupt archive is ignored`() {
        val corrupt = Files.createTempFile("jvmdg-corrupt", ".jar")
        Files.writeString(corrupt, "not a zip")

        assertEquals(JvmDowngraderJarInfo(false, emptySet()), JvmDowngraderJarScanner.scan(corrupt))
    }

    private fun fixtureJar(vararg entries: Pair<String, ByteArray>): Path {
        val path = Files.createTempFile("jvmdg-fixture", ".jar")
        ZipOutputStream(Files.newOutputStream(path)).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        return path
    }
}
