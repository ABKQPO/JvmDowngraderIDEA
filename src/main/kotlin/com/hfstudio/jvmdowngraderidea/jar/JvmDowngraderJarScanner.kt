package com.hfstudio.jvmdowngraderidea.jar

import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipFile

object JvmDowngraderJarScanner {
    private const val MAX_CLASS_BYTES = 1_048_576
    private const val JVMDG_MARKER = "xyz/wagyourtail/jvmdg/"
    private val multiReleaseEntry = Regex("^META-INF/versions/(\\d+)/")

    fun scan(path: Path): JvmDowngraderJarInfo = try {
        ZipFile(path.toFile()).use { zip ->
            val versions = zip.entries().asSequence()
                .mapNotNull { entry -> multiReleaseEntry.find(entry.name)?.groupValues?.get(1)?.toIntOrNull() }
                .toSortedSet()
            val hasMarker = zip.entries().asSequence()
                .filter { entry -> !entry.isDirectory && entry.size in 1..MAX_CLASS_BYTES }
                .any { entry ->
                    zip.getInputStream(entry).use { input ->
                        input.readNBytes(MAX_CLASS_BYTES).containsAscii(JVMDG_MARKER)
                    }
                }
            JvmDowngraderJarInfo(hasMarker, versions)
        }
    } catch (_: IOException) {
        JvmDowngraderJarInfo(false, emptySet())
    }

    private fun ByteArray.containsAscii(needle: String): Boolean {
        val bytes = needle.encodeToByteArray()
        return indices.any { start ->
            start + bytes.size <= size && bytes.indices.all { offset -> this[start + offset] == bytes[offset] }
        }
    }
}
