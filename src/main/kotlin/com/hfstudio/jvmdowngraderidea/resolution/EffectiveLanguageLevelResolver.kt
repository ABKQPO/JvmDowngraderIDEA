package com.hfstudio.jvmdowngraderidea.resolution

import com.hfstudio.jvmdowngraderidea.detection.ModernJavaDetection
import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode

sealed interface EffectiveLanguageLevel {
    data class Ready(val version: Int) : EffectiveLanguageLevel
    data class MissingSdk(val requiredVersion: Int) : EffectiveLanguageLevel
    data object Unmanaged : EffectiveLanguageLevel
}

object EffectiveLanguageLevelResolver {
    fun resolve(
        projectMode: LanguageLevelMode,
        globalMode: LanguageLevelMode,
        detection: ModernJavaDetection,
        availableSdkVersions: Set<Int>,
    ): EffectiveLanguageLevel {
        val selectedMode = if (projectMode == LanguageLevelMode.INHERIT_GLOBAL) globalMode else projectMode
        val automaticVersion = if (selectedMode == LanguageLevelMode.AUTO) {
            detection.detectedSourceVersion
        } else {
            null
        }
        val version = selectedMode.javaVersion ?: automaticVersion ?: return EffectiveLanguageLevel.Unmanaged

        return if (availableSdkVersions.any { it >= version }) {
            EffectiveLanguageLevel.Ready(version)
        } else {
            EffectiveLanguageLevel.MissingSdk(version)
        }
    }
}
