package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.JvmDowngraderBundle
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
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.util.Consumer
import java.awt.Dimension
import java.awt.Point
import java.awt.event.MouseEvent

class LibrarySourceLanguageLevelWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = ID

    override fun getDisplayName(): String = JvmDowngraderBundle.message("popup.title")

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
        JvmDowngraderBundle.message("status.not-dependency")
    } else {
        JvmDowngraderBundle.message("status.choose-level")
    }

    override fun getAlignment(): Float = 0.5f

    @Suppress("RedundantNullableReturnType")
    override fun getClickConsumer(): Consumer<MouseEvent>? {
        return Consumer { event ->
            currentDependencySource()?.let { showChoices(it, event) }
        }
    }

    override fun dispose() {
        statusBar = null
    }

    private fun currentFile() = fileEditorManager.currentFile

    private fun currentDependencySource() = ReadAction.computeBlocking<com.intellij.openapi.vfs.VirtualFile?, RuntimeException> {
        currentFile()?.takeIf {
            it.extension == "java" && ProjectFileIndex.getInstance(project).isInLibrarySource(it)
        }
    }

    private fun showChoices(file: com.intellij.openapi.vfs.VirtualFile, event: MouseEvent) {
        val choices = LanguageLevelMode.entries.filter { it != LanguageLevelMode.INHERIT_GLOBAL }
        val popup = JBPopupFactory.getInstance()
            .createPopupChooserBuilder(choices)
            .setRenderer(SimpleListCellRenderer.create("") { it?.presentableName })
            .setTitle(JvmDowngraderBundle.message("popup.title"))
            .setItemChosenCallback { mode ->
                project.service<LibrarySourceRefreshCoordinator>().setFileOverride(file, mode)
                update()
            }
            .createPopup()
        popup.pack(true, true)
        val buttonLocation = event.component.locationOnScreen
        val popupSize = popup.content.preferredSize ?: Dimension(240, 180)
        val x = buttonLocation.x + (event.component.width - popupSize.width) / 2
        val y = buttonLocation.y - popupSize.height
        popup.showInScreenCoordinates(event.component, Point(x, y))
    }

    private fun update() {
        statusBar?.updateWidget(ID())
    }
}
