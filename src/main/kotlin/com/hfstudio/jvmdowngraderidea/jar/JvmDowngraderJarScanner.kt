package com.hfstudio.jvmdowngraderidea.jar

import java.io.IOException
import java.nio.file.Path
import java.util.zip.ZipFile

object JvmDowngraderJarScanner {
    private const val MAX_CLASS_BYTES = 1_048_576
    private const val JVMDG_MARKER = "xyz/wagyourtail/jvmdg/"
    private val jvmdgMarkerBytes = JVMDG_MARKER.encodeToByteArray()
    private val multiReleaseEntry = Regex("^META-INF/versions/(\\d+)/")

    fun scan(path: Path): JvmDowngraderJarInfo = try {
        ZipFile(path.toFile()).use { zip ->
            val versions = sortedSetOf<Int>()
            var hasMarker = false
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                multiReleaseEntry.find(entry.name)?.groupValues?.get(1)?.toIntOrNull()?.let(versions::add)
                if (!hasMarker && entry.name.endsWith(".class") && entry.size in 1..MAX_CLASS_BYTES) {
                    zip.getInputStream(entry).use { input ->
                        hasMarker = input.readNBytes(MAX_CLASS_BYTES).containsAscii(jvmdgMarkerBytes)
                    }
                }
            }
            JvmDowngraderJarInfo(hasMarker, versions)
        }
    } catch (_: IOException) {
        JvmDowngraderJarInfo(false, emptySet())
    }

    private fun ByteArray.containsAscii(needle: ByteArray): Boolean {
        if (needle.size > size) return false

        val finalIndex = needle.lastIndex
        val shifts = IntArray(256) { needle.size }
        for (index in 0 until finalIndex) {
            shifts[needle[index].toInt() and 0xff] = finalIndex - index
        }

        var start = 0
        while (start <= size - needle.size) {
            var index = finalIndex
            while (index >= 0 && this[start + index] == needle[index]) index--
            if (index < 0) return true
            start += shifts[this[start + finalIndex].toInt() and 0xff]
        }
        return false
    }
}
