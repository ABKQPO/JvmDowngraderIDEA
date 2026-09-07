package com.hfstudio.jvmdowngraderidea.detection

object GradleModernJavaDetector {
    fun detect(gradleProperties: String, daemonProperties: String): ModernJavaDetection {
        val properties = GradlePropertiesParser.parse(gradleProperties)
        val daemonPropertiesMap = GradlePropertiesParser.parse(daemonProperties)
        val toolchain = (properties["forceToolchainVersion"] ?: daemonPropertiesMap["toolchainVersion"])
            ?.toJavaVersionOrNull()

        return when (properties["enableModernJavaSyntax"]?.trim()) {
            "jabel" -> ModernJavaDetection(
                mode = ModernJavaMode.JABEL,
                detectedSourceVersion = toolchain,
                downgradeTargetVersion = null,
                multiReleaseVersions = emptySet(),
            )

            "jvmDowngrader" -> {
                val multiReleaseVersions = properties["jvmDowngraderMultiReleaseVersions"]
                    ?.split(',')
                    ?.mapNotNull { value -> value.toJavaVersionOrNull() }
                    ?.toSortedSet()
                    .orEmpty()
                ModernJavaDetection(
                    mode = ModernJavaMode.JVM_DOWNGRADER,
                    detectedSourceVersion = multiReleaseVersions.maxOrNull() ?: toolchain,
                    downgradeTargetVersion = properties["downgradeTargetVersion"]?.toJavaVersionOrNull(),
                    multiReleaseVersions = multiReleaseVersions,
                )
            }

            else -> ModernJavaDetection(ModernJavaMode.NONE, null, null, emptySet())
        }
    }

    private fun String.toJavaVersionOrNull(): Int? = trim().toIntOrNull()?.takeIf { it >= 8 }
}
