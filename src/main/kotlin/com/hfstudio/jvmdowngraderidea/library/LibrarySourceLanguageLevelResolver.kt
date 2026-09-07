package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.jar.JvmDowngraderJarInfo
import com.hfstudio.jvmdowngraderidea.jar.JvmDowngraderJarScanner
import com.hfstudio.jvmdowngraderidea.source.JavaSourceLanguageFeatureDetector
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object LibrarySourceLanguageLevelResolver {
    private val jarInfoCache = ConcurrentHashMap<Path, JvmDowngraderJarInfo>()

    fun resolve(sourceFile: VirtualFile, library: Library, fixedLevel: Int?): Int? {
        if (fixedLevel != null) return fixedLevel

        val jvmDowngraderLevel = library.getFiles(OrderRootType.CLASSES)
            .asSequence()
            .mapNotNull(::jarInfo)
            .mapNotNull(JvmDowngraderJarInfo::automaticLanguageLevel)
            .maxOrNull()
        if (jvmDowngraderLevel != null) return jvmDowngraderLevel

        val sourceSyntaxLevel = runCatching {
            JavaSourceLanguageFeatureDetector.detect(VfsUtilCore.loadText(sourceFile))
        }.getOrNull()
        return selectLevel(null, null, sourceSyntaxLevel)
    }

    fun selectLevel(
        fixedLevel: Int?,
        jvmDowngraderLevel: Int?,
        sourceSyntaxLevel: Int?,
    ): Int? = fixedLevel ?: jvmDowngraderLevel ?: sourceSyntaxLevel

    private fun jarInfo(classRoot: VirtualFile): JvmDowngraderJarInfo? {
        val jarFile = VfsUtilCore.getVirtualFileForJar(classRoot) ?: return null
        val path = runCatching { Path.of(jarFile.path).toAbsolutePath().normalize() }.getOrNull() ?: return null
        return jarInfoCache.computeIfAbsent(path, JvmDowngraderJarScanner::scan)
    }
}
