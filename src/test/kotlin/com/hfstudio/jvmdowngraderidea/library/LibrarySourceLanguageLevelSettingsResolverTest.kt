package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibrarySourceLanguageLevelSettingsResolverTest {
    @Test
    fun `project fixed level overrides global fixed level`() {
        assertEquals(
            17,
            LibrarySourceLanguageLevelSettingsResolver.fixedLevel(
                projectMode = LanguageLevelMode.JAVA_17,
                globalMode = LanguageLevelMode.JAVA_25,
            ),
        )
    }

    @Test
    fun `project inherit uses global fixed level`() {
        assertEquals(
            25,
            LibrarySourceLanguageLevelSettingsResolver.fixedLevel(
                projectMode = LanguageLevelMode.INHERIT_GLOBAL,
                globalMode = LanguageLevelMode.JAVA_25,
            ),
        )
    }

    @Test
    fun `automatic project mode overrides a global fixed level`() {
        assertNull(
            LibrarySourceLanguageLevelSettingsResolver.fixedLevel(
                projectMode = LanguageLevelMode.AUTO,
                globalMode = LanguageLevelMode.JAVA_25,
            ),
        )
    }
}
