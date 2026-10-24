package com.hfstudio.jvmdowngraderidea.library

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.wm.WindowManager
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.psi.PsiManager
import com.intellij.util.FileContentUtilCore
import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderApplicationSettings
import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderProjectSettings
import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class LibrarySourceRefreshCoordinator(private val project: Project) {
    private val pusher = LibraryJavaLanguageLevelPusher()
    private val pendingResolutionUrls = ConcurrentHashMap.newKeySet<String>()
    private val fileOverrides = ConcurrentHashMap<String, LanguageLevelMode>()

    fun setFileOverride(file: VirtualFile, mode: LanguageLevelMode?) {
        if (mode == null || mode == LanguageLevelMode.AUTO) {
            fileOverrides.remove(file.url)
            LibrarySourceLanguageLevelOverrides.set(project, file, null)
        } else {
            fileOverrides[file.url] = mode
            LibrarySourceLanguageLevelOverrides.set(project, file, mode.javaVersion)
        }
        scheduleUpdate(file)
    }

    fun fileOverride(file: VirtualFile): LanguageLevelMode =
        fileOverrides[file.url] ?: LanguageLevelMode.AUTO

    fun statusText(file: VirtualFile): String {
        val override = fileOverrides[file.url]
        if (override != null && override != LanguageLevelMode.AUTO) return override.presentableName

        val detected = pusher.lookup(file).level ?: 8
        return "Auto ($detected)"
    }

    fun start() {
        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) {
                scheduleUpdate(file)
            }
        })
        refresh()
    }

    fun refresh() {
        ApplicationManager.getApplication().invokeLater(
            {
                if (project.isDisposed) return@invokeLater
                FileEditorManager.getInstance(project).openFiles.forEach(::updateOrRequest)
            },
            ModalityState.nonModal(),
        )
    }

    private fun scheduleUpdate(file: com.intellij.openapi.vfs.VirtualFile) {
        ApplicationManager.getApplication().invokeLater(
            {
                if (!project.isDisposed) {
                    updateOrRequest(file)
                    WindowManager.getInstance().getStatusBar(project)
                        ?.updateWidget(LibrarySourceLanguageLevelWidgetFactory.ID)
                }
            },
            ModalityState.nonModal(),
        )
    }

    private fun updateOrRequest(file: com.intellij.openapi.vfs.VirtualFile) {
        if (!LibrarySourceRefreshScope.shouldUpdate(
                file.extension,
                ProjectFileIndex.getInstance(project).isInLibrarySource(file),
            )
        ) {
            return
        }

        val fileOverride = fileOverrides[file.url]
        fileOverride?.javaVersion?.let {
            updateLanguageLevel(file, com.intellij.pom.java.LanguageLevel.forFeature(it))
            return
        }

        val fixedLevel = LibrarySourceLanguageLevelSettingsResolver.fixedLevel(
            project.service<JvmDowngraderProjectSettings>().mode,
            service<JvmDowngraderApplicationSettings>().mode,
        )
        if (fixedLevel != null) {
            updateLanguageLevel(file, com.intellij.pom.java.LanguageLevel.forFeature(fixedLevel))
            return
        }

        val lookup = pusher.lookup(file)
        if (!lookup.isComplete) {
            if (pendingResolutionUrls.add(file.url)) {
                pusher.requestResolution(project, file) {
                    pendingResolutionUrls.remove(file.url)
                    scheduleUpdate(file)
                }
            }
            return
        }

        updateLanguageLevel(file, lookup.level?.let(com.intellij.pom.java.LanguageLevel::forFeature))
    }

    private fun updateLanguageLevel(file: com.intellij.openapi.vfs.VirtualFile, level: com.intellij.pom.java.LanguageLevel?) {
        if (pusher.filePropertyKey.getPersistentValue(file) == level) return

        if (level == null) {
            pusher.filePropertyKey.setPersistentValue(file, null)
        } else {
            // Java PSI reads this exact persistent key when calculating the
            // language level. Write it directly so the built-in Java pusher
            // cannot immediately replace a manual dependency-source choice.
            pusher.filePropertyKey.setPersistentValue(file, level)
        }
        // Changing the persistent property does not invalidate an already
        // created Java PSI file. Force a VFS reparse so the new level is used
        // immediately, without requiring a sandbox restart.
        FileContentUtilCore.reparseFiles(file)
        PsiManager.getInstance(project).findFile(file)?.let {
            DaemonCodeAnalyzer.getInstance(project).restart(it)
        }
    }
}
