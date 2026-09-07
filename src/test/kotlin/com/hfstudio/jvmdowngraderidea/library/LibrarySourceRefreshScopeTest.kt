package com.hfstudio.jvmdowngraderidea.library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySourceRefreshScopeTest {
    @Test
    fun `project Java sources are excluded from library refreshes`() {
        assertFalse(LibrarySourceRefreshScope.shouldUpdate("java", isLibrarySource = false))
    }

    @Test
    fun `Java library sources are included in library refreshes`() {
        assertTrue(LibrarySourceRefreshScope.shouldUpdate("java", isLibrarySource = true))
    }
}
