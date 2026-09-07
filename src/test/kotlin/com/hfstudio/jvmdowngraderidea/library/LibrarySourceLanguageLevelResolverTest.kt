package com.hfstudio.jvmdowngraderidea.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibrarySourceLanguageLevelResolverTest {
    @Test
    fun `fixed setting overrides artifact automatic level`() {
        assertEquals(
            17,
            LibrarySourceLanguageLevelResolver.selectLevel(
                fixedLevel = 17,
                jvmDowngraderLevel = 25,
                sourceSyntaxLevel = 21,
            ),
        )
    }

    @Test
    fun `jvm downgrader archive wins over source fallback`() {
        assertEquals(
            25,
            LibrarySourceLanguageLevelResolver.selectLevel(
                fixedLevel = null,
                jvmDowngraderLevel = 25,
                sourceSyntaxLevel = 21,
            ),
        )
    }

    @Test
    fun `jabel source syntax supplies automatic level without archive metadata`() {
        assertEquals(
            16,
            LibrarySourceLanguageLevelResolver.selectLevel(
                fixedLevel = null,
                jvmDowngraderLevel = null,
                sourceSyntaxLevel = 16,
            ),
        )
    }

    @Test
    fun `ordinary Java 8 source has no override in automatic mode`() {
        assertNull(
            LibrarySourceLanguageLevelResolver.selectLevel(
                fixedLevel = null,
                jvmDowngraderLevel = null,
                sourceSyntaxLevel = null,
            ),
        )
    }
}
