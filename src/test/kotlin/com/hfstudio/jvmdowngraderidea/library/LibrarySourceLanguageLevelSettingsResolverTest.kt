package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode
import org.junit.Assert.assertEquals
import org.junit.Test

class LibrarySourceLanguageLevelSettingsResolverTest {
    @Test
    fun `project default mode is selected unless it inherits global`() {
        assertEquals(
            LanguageLevelMode.JAVA_8,
            LibrarySourceLanguageLevelSettingsResolver.defaultMode(
                projectMode = LanguageLevelMode.JAVA_8,
                globalMode = LanguageLevelMode.JAVA_25,
            ),
        )
        assertEquals(
            LanguageLevelMode.JAVA_25,
            LibrarySourceLanguageLevelSettingsResolver.defaultMode(
                projectMode = LanguageLevelMode.INHERIT_GLOBAL,
                globalMode = LanguageLevelMode.JAVA_25,
            ),
        )
    }

    @Test
    fun `automatic default remains automatic`() {
        assertEquals(
            LanguageLevelMode.AUTO,
            LibrarySourceLanguageLevelSettingsResolver.defaultMode(
                projectMode = LanguageLevelMode.INHERIT_GLOBAL,
                globalMode = LanguageLevelMode.AUTO,
            ),
        )
    }

}
