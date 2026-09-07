package com.hfstudio.jvmdowngraderidea.detection

data class ModernJavaDetection(
    val mode: ModernJavaMode,
    val detectedSourceVersion: Int?,
    val downgradeTargetVersion: Int?,
    val multiReleaseVersions: Set<Int>,
)
