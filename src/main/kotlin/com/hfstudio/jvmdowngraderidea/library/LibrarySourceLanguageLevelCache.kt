package com.hfstudio.jvmdowngraderidea.library

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/** Stores only results that are safe for the file-property pusher to read synchronously. */
internal class LibrarySourceLanguageLevelCache {
    private val results = ConcurrentHashMap<String, CompletableFuture<Int?>>()

    fun lookup(sourceUrl: String): LibrarySourceLanguageLevelLookup {
        val result = results[sourceUrl] ?: return LibrarySourceLanguageLevelLookup.pending()
        if (!result.isDone) return LibrarySourceLanguageLevelLookup.pending()
        return LibrarySourceLanguageLevelLookup.resolved(runCatching { result.getNow(null) }.getOrNull())
    }

    fun acquire(sourceUrl: String): Acquisition {
        val candidate = CompletableFuture<Int?>()
        val existing = results.putIfAbsent(sourceUrl, candidate)
        return Acquisition(existing ?: candidate, existing == null)
    }

    fun complete(sourceUrl: String, level: Int?) {
        results.computeIfAbsent(sourceUrl) { CompletableFuture() }.complete(level)
    }

    data class Acquisition(val future: CompletableFuture<Int?>, val isNew: Boolean)
}
