package com.hfstudio.jvmdowngraderidea.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySourceLanguageLevelCacheTest {
    @Test
    fun `unseen source has no synchronously available language level`() {
        val cache = LibrarySourceLanguageLevelCache()

        val lookup = cache.lookup("jar://dependency-sources.jar!/example/Dependency.java")

        assertFalse(lookup.isComplete)
        assertNull(lookup.level)
    }

    @Test
    fun `completed source result is available without repeating detection`() {
        val cache = LibrarySourceLanguageLevelCache()
        val sourceUrl = "jar://dependency-sources.jar!/example/Dependency.java"
        cache.complete(sourceUrl, 21)

        val lookup = cache.lookup(sourceUrl)

        assertTrue(lookup.isComplete)
        assertEquals(21, lookup.level)
    }
}
