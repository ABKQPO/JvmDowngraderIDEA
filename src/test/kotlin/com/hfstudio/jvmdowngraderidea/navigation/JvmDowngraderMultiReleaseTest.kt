package com.hfstudio.jvmdowngraderidea.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JvmDowngraderMultiReleaseTest {
    @Test
    fun `versioned entries map to the jar root path`() {
        assertEquals("com/example/Foo.class", JvmDowngraderMultiRelease.basePath("META-INF/versions/17/com/example/Foo.class"))
        assertEquals("Foo\$Inner.class", JvmDowngraderMultiRelease.basePath("META-INF/versions/9/Foo\$Inner.class"))
    }

    @Test
    fun `root entries are not versioned`() {
        assertNull(JvmDowngraderMultiRelease.basePath("com/example/Foo.class"))
        assertNull(JvmDowngraderMultiRelease.basePath("META-INF/MANIFEST.MF"))
    }

    @Test
    fun `versions below the multi-release minimum are ignored`() {
        assertNull(JvmDowngraderMultiRelease.basePath("META-INF/versions/8/com/example/Foo.class"))
    }

    @Test
    fun `non-numeric version directories are ignored`() {
        assertNull(JvmDowngraderMultiRelease.basePath("META-INF/versions/abc/com/example/Foo.class"))
        assertNull(JvmDowngraderMultiRelease.basePath("META-INF/versions/17/"))
    }
}
