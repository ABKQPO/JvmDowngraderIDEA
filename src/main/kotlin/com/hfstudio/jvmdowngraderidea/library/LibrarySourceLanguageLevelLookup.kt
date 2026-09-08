package com.hfstudio.jvmdowngraderidea.library

data class LibrarySourceLanguageLevelLookup(
    val level: Int?,
    val isComplete: Boolean,
) {
    companion object {
        fun fixed(level: Int): LibrarySourceLanguageLevelLookup = LibrarySourceLanguageLevelLookup(level, true)

        fun pending(): LibrarySourceLanguageLevelLookup = LibrarySourceLanguageLevelLookup(null, false)

        fun resolved(level: Int?): LibrarySourceLanguageLevelLookup = LibrarySourceLanguageLevelLookup(level, true)
    }
}
