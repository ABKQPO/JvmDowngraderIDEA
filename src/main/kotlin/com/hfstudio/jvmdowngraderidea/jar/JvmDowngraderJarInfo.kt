package com.hfstudio.jvmdowngraderidea.jar

data class JvmDowngraderJarInfo(
    val isJvmDowngrader: Boolean,
    val multiReleaseVersions: Set<Int>,
) {
    val automaticLanguageLevel: Int?
        get() = multiReleaseVersions.maxOrNull().takeIf { isJvmDowngrader }
}
