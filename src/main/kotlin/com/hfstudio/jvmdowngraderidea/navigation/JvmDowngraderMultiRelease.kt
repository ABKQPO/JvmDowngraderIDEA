package com.hfstudio.jvmdowngraderidea.navigation

import com.hfstudio.jvmdowngraderidea.library.LibrarySourceLanguageLevelResolver
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

/**
 * JVM Downgrader jars keep the original classes under META-INF/versions/N and
 * the downgraded ones in the jar root. IntelliJ resolves the versioned classes
 * for modern language levels, but their sources live at the base path.
 */
object JvmDowngraderMultiRelease {
    private const val MIN_MULTI_RELEASE_VERSION = 9
    private val versionedEntry = Regex("^META-INF/versions/(\\d+)/(.+)$")

    /** Returns the jar-root path for a versioned entry, or null when [relativePath] is not versioned. */
    fun basePath(relativePath: String): String? {
        val match = versionedEntry.matchEntire(relativePath) ?: return null
        val version = match.groupValues[1].toIntOrNull() ?: return null
        return match.groupValues[2].takeIf { version >= MIN_MULTI_RELEASE_VERSION }
    }

    fun isVersionedClassFile(file: VirtualFile): Boolean = versionedBasePath(file) != null

    /**
     * Returns the base class file for a versioned class of a JVM Downgrader jar.
     * Returns null for other jars and while the jar has not been scanned yet.
     */
    fun findBaseClassFile(file: VirtualFile): VirtualFile? {
        val basePath = versionedBasePath(file) ?: return null
        val root = VfsUtilCore.getRootFile(file)
        if (LibrarySourceLanguageLevelResolver.cachedJarInfo(root)?.isJvmDowngrader != true) return null
        return root.findFileByRelativePath(basePath)
    }

    private fun versionedBasePath(file: VirtualFile): String? {
        if (file.extension != "class" || file.fileSystem !is JarFileSystem) return null
        val root = VfsUtilCore.getRootFile(file)
        return VfsUtilCore.getRelativePath(file, root)?.let(::basePath)
    }
}
