package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderApplicationSettings
import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderProjectSettings
import com.intellij.FilePropertyPusherBase
import com.intellij.openapi.components.service
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
        file?.let { resolve(project, it) }

    override fun acceptsFile(file: VirtualFile, project: Project): Boolean = resolve(project, file) != null

    override fun acceptsDirectory(file: VirtualFile, project: Project): Boolean = false

    override fun propertyChanged(project: Project, file: VirtualFile, value: LanguageLevel) {
        // PushedFilePropertiesUpdater can invoke this from a write-unsafe context.
        // The refresh coordinator invalidates PSI after all properties are updated.
    }

    fun resolve(project: Project, file: VirtualFile): LanguageLevel? {
        if (file.extension != "java") return null

        val fileIndex = ProjectFileIndex.getInstance(project)
        if (!fileIndex.isInLibrarySource(file)) return null

        val fixedLevel = LibrarySourceLanguageLevelSettingsResolver.fixedLevel(
            project.service<JvmDowngraderProjectSettings>().mode,
            service<JvmDowngraderApplicationSettings>().mode,
        )
        return fileIndex.getOrderEntriesForFile(file)
            .asSequence()
            .filterIsInstance<LibraryOrderEntry>()
            .mapNotNull(LibraryOrderEntry::getLibrary)
            .mapNotNull { library -> LibrarySourceLanguageLevelResolver.resolve(file, library, fixedLevel) }
            .maxOrNull()
            ?.let(LanguageLevel::forFeature)
    }
}
