package com.hfstudio.jvmdowngraderidea.jar

data class JvmDowngraderJarInfo(
    val isJvmDowngrader: Boolean,
    val multiReleaseVersions: Set<Int>,
)
