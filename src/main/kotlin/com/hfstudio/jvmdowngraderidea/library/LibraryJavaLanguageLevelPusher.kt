package com.hfstudio.jvmdowngraderidea.library

import com.intellij.FilePropertyPusherBase
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.impl.JavaLanguageLevelPusher
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.FilePropertyKey

class LibraryJavaLanguageLevelPusher : FilePropertyPusherBase<LanguageLevel>() {
    private val languageLevelKey = JavaLanguageLevelPusher().filePropertyKey

    override fun getFilePropertyKey(): FilePropertyKey<LanguageLevel> = languageLevelKey

    override fun pushDirectoriesOnly(): Boolean = false

    override fun getDefaultValue(): LanguageLevel = LanguageLevel.HIGHEST

    override fun getImmediateValue(module: Module): LanguageLevel = LanguageLevel.HIGHEST

    override fun getImmediateValue(project: Project, file: VirtualFile?): LanguageLevel? =
        file?.let { lookup(it).level?.let(LanguageLevel::forFeature) }

    override fun acceptsFile(file: VirtualFile, project: Project): Boolean = lookup(file).level != null

    override fun acceptsDirectory(file: VirtualFile, project: Project): Boolean = false

    override fun propertyChanged(project: Project, file: VirtualFile, value: LanguageLevel) {
        // PushedFilePropertiesUpdater can invoke this from a write-unsafe context.
        // The refresh coordinator invalidates PSI after all properties are updated.
    }

    fun lookup(file: VirtualFile): LibrarySourceLanguageLevelLookup {
        if (file.extension != "java") return LibrarySourceLanguageLevelLookup.resolved(null)
        return LibrarySourceLanguageLevelResolver.cachedLevel(file)
    }

    fun requestResolution(project: Project, file: VirtualFile, onResolved: () -> Unit) {
        if (file.extension != "java") return

        val fileIndex = ProjectFileIndex.getInstance(project)
        if (!fileIndex.isInLibrarySource(file)) return

        val libraries = fileIndex.getOrderEntriesForFile(file)
            .filterIsInstance<LibraryOrderEntry>()
            .mapNotNull(LibraryOrderEntry::getLibrary)
        LibrarySourceLanguageLevelResolver.requestLevel(file, libraries, onResolved)
    }
}
