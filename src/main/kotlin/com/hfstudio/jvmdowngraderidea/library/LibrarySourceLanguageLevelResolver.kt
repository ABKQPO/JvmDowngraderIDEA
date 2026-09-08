package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.jar.JvmDowngraderJarInfo
import com.hfstudio.jvmdowngraderidea.jar.JvmDowngraderJarScanner
import com.hfstudio.jvmdowngraderidea.source.JavaSourceLanguageFeatureDetector
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object LibrarySourceLanguageLevelResolver {
    private val jarInfoCache = ConcurrentHashMap<Path, CompletableFuture<JvmDowngraderJarInfo>>()
    private val sourceLevelCache = LibrarySourceLanguageLevelCache()

    fun cachedLevel(sourceFile: VirtualFile): LibrarySourceLanguageLevelLookup = sourceLevelCache.lookup(sourceFile.url)

    fun requestLevel(
        sourceFile: VirtualFile,
        libraries: Collection<Library>,
        onResolved: () -> Unit,
    ) {
        val sourceResult = sourceLevelCache.acquire(sourceFile.url)
        sourceResult.future.whenComplete { _, _ -> onResolved() }
        if (!sourceResult.isNew) return

        val jarFutures = libraries.asSequence()
            .flatMap { it.getFiles(OrderRootType.CLASSES).asSequence() }
            .mapNotNull(::jarPath)
            .map(::requestJarInfo)
            .toList()
        CompletableFuture.allOf(*jarFutures.toTypedArray()).whenComplete { _, _ ->
            val jvmDowngraderLevel = jarFutures.asSequence()
                .mapNotNull { runCatching { it.getNow(null) }.getOrNull() }
                .mapNotNull(JvmDowngraderJarInfo::automaticLanguageLevel)
                .maxOrNull()
            if (jvmDowngraderLevel != null) {
                sourceResult.future.complete(jvmDowngraderLevel)
            } else {
                requestSourceLevel(sourceFile, sourceResult.future)
            }
        }
    }

    fun selectLevel(
        fixedLevel: Int?,
        jvmDowngraderLevel: Int?,
        sourceSyntaxLevel: Int?,
    ): Int? = fixedLevel ?: jvmDowngraderLevel ?: sourceSyntaxLevel

    private fun requestJarInfo(path: Path): CompletableFuture<JvmDowngraderJarInfo> =
        jarInfoCache.computeIfAbsent(path) {
            CompletableFuture<JvmDowngraderJarInfo>().also { result ->
                AppExecutorUtil.getAppExecutorService().execute {
                    result.complete(JvmDowngraderJarScanner.scan(path))
                }
            }
        }

    private fun requestSourceLevel(sourceFile: VirtualFile, result: CompletableFuture<Int?>) {
        AppExecutorUtil.getAppExecutorService().execute {
            result.complete(
                runCatching { JavaSourceLanguageFeatureDetector.detect(VfsUtilCore.loadText(sourceFile)) }
                    .getOrNull(),
            )
        }
    }

    private fun jarPath(classRoot: VirtualFile): Path? {
        val jarFile = VfsUtilCore.getVirtualFileForJar(classRoot) ?: return null
        return runCatching { Path.of(jarFile.path).toAbsolutePath().normalize() }.getOrNull()
    }
}
