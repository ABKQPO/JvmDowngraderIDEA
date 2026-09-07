package com.hfstudio.jvmdowngraderidea.library

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.roots.impl.PushedFilePropertiesUpdater
import com.intellij.psi.PsiManager

@Service(Service.Level.PROJECT)
class LibrarySourceRefreshCoordinator(private val project: Project) {
    private val pusher = LibraryJavaLanguageLevelPusher()

    fun start() {
        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: com.intellij.openapi.vfs.VirtualFile) {
                update(file)
            }
        })
        refresh()
    }

    fun refresh() {
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater
            FileEditorManager.getInstance(project).openFiles.forEach(::update)
            PsiManager.getInstance(project).dropPsiCaches()
            DaemonCodeAnalyzer.getInstance(project).restart(project)
        }
    }

    private fun update(file: com.intellij.openapi.vfs.VirtualFile) {
        val level = pusher.resolve(project, file)
        if (level == null) {
            pusher.filePropertyKey.setPersistentValue(file, null)
        } else {
            PushedFilePropertiesUpdater.getInstance(project).findAndUpdateValue(file, pusher, level)
        }
    }
}
