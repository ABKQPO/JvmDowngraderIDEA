package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

class LibrarySourceLanguageLevelWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = ID

    override fun getDisplayName(): String = "Dependency Source Language Level"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget = LibrarySourceLanguageLevelWidget(project)

    override fun disposeWidget(widget: StatusBarWidget) = widget.dispose()

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

    companion object {
        const val ID = "JvmDowngraderLibrarySourceLanguageLevel"
    }
}

private class LibrarySourceLanguageLevelWidget(
    private val project: Project,
) : StatusBarWidget, StatusBarWidget.TextPresentation, Disposable {
    private var statusBar: StatusBar? = null
    private val fileEditorManager = FileEditorManager.getInstance(project)
    private val listener = object : FileEditorManagerListener {
        override fun selectionChanged(event: FileEditorManagerEvent) = update()
    }

    override fun ID(): String = LibrarySourceLanguageLevelWidgetFactory.ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
        project.messageBus.connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, listener)
        update()
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String = currentDependencySource()?.let {
        project.service<LibrarySourceRefreshCoordinator>().statusText(it)
    } ?: "JvmDg"

    override fun getTooltipText(): String = if (currentDependencySource() == null) {
        "Current file is not a dependency source"
    } else {
        "Choose Java language level for this dependency source"
    }

    override fun getAlignment(): Float = 0.5f

    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return Consumer { event ->
            currentDependencySource()?.let { showChoices(it, event) }
        }
    }

    override fun dispose() {
        statusBar = null
    }

    private fun currentFile() = fileEditorManager.currentFile

    private fun currentDependencySource() = ReadAction.compute<com.intellij.openapi.vfs.VirtualFile?, RuntimeException> {
        currentFile()?.takeIf {
            it.extension == "java" && ProjectFileIndex.getInstance(project).isInLibrarySource(it)
        }
    }

    private fun showChoices(file: com.intellij.openapi.vfs.VirtualFile, event: MouseEvent) {
        val choices = listOf(
            LanguageLevelMode.AUTO,
            LanguageLevelMode.JAVA_8,
            LanguageLevelMode.JAVA_17,
            LanguageLevelMode.JAVA_25,
        )
        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(choices)
            .setTitle("Dependency Source Language Level")
            .setItemChosenCallback { mode ->
                project.service<LibrarySourceRefreshCoordinator>().setFileOverride(file, mode)
                update()
            }
            .createPopup()
            .showInScreenCoordinates(event.component, event.locationOnScreen)
    }

    private fun update() {
        statusBar?.updateWidget(ID())
    }
}
