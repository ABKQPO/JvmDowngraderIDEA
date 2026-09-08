package com.hfstudio.jvmdowngraderidea.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySourceLanguageLevelLookupTest {
    @Test
    fun `pending lookup does not expose an unverified language level`() {
        val lookup = LibrarySourceLanguageLevelLookup.pending()

        assertFalse(lookup.isComplete)
        assertNull(lookup.level)
    }

    @Test
    fun `fixed level is immediately complete without detection`() {
        val lookup = LibrarySourceLanguageLevelLookup.fixed(21)

        assertTrue(lookup.isComplete)
        assertEquals(21, lookup.level)
    }
}
