package com.hfstudio.jvmdowngraderidea.resolution

import com.hfstudio.jvmdowngraderidea.detection.ModernJavaDetection
import com.hfstudio.jvmdowngraderidea.detection.ModernJavaMode
import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode
import org.junit.Assert.assertEquals
import org.junit.Test

class EffectiveLanguageLevelResolverTest {
    private val jvmDowngrader = ModernJavaDetection(
        mode = ModernJavaMode.JVM_DOWNGRADER,
        detectedSourceVersion = 25,
        downgradeTargetVersion = 8,
        multiReleaseVersions = setOf(17, 21, 25)
    )

    @Test
    fun `project override wins over global override`() {
        assertEquals(
            EffectiveLanguageLevel.Ready(21),
            EffectiveLanguageLevelResolver.resolve(
                projectMode = LanguageLevelMode.JAVA_21,
                globalMode = LanguageLevelMode.JAVA_25,
                detection = jvmDowngrader,
                availableSdkVersions = setOf(17, 21, 25)
            )
        )
    }

    @Test
    fun `project inherit global auto uses Gradle detection`() {
        assertEquals(
            EffectiveLanguageLevel.Ready(25),
            EffectiveLanguageLevelResolver.resolve(
                projectMode = LanguageLevelMode.INHERIT_GLOBAL,
                globalMode = LanguageLevelMode.AUTO,
                detection = jvmDowngrader,
                availableSdkVersions = setOf(25)
            )
        )
    }

    @Test
    fun `project auto overrides global fixed level`() {
        assertEquals(
            EffectiveLanguageLevel.Ready(25),
            EffectiveLanguageLevelResolver.resolve(
                projectMode = LanguageLevelMode.AUTO,
                globalMode = LanguageLevelMode.JAVA_21,
                detection = jvmDowngrader,
                availableSdkVersions = setOf(25)
            )
        )
    }

    @Test
    fun `missing compatible SDK leaves configuration unresolved`() {
        assertEquals(
            EffectiveLanguageLevel.MissingSdk(25),
            EffectiveLanguageLevelResolver.resolve(
                projectMode = LanguageLevelMode.INHERIT_GLOBAL,
                globalMode = LanguageLevelMode.AUTO,
                detection = jvmDowngrader,
                availableSdkVersions = setOf(17, 21)
            )
        )
    }
}
