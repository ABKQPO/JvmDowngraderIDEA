package com.hfstudio.jvmdowngraderidea.detection

object GradlePropertiesParser {
    fun parse(content: String): Map<String, String> = buildMap {
        content.lineSequence()
            .map(String::trim)
            .filter { it.isNotEmpty() && !it.startsWith('#') && !it.startsWith('!') }
            .forEach { line ->
                val separator = line.indexOfFirst { it == '=' || it == ':' }
                if (separator > 0) {
                    put(line.substring(0, separator).trim(), line.substring(separator + 1).trim())
                }
            }
    }
}
