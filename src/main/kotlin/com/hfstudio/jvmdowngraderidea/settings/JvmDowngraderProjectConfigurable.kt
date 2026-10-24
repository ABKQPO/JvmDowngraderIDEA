package com.hfstudio.jvmdowngraderidea.settings

import com.hfstudio.jvmdowngraderidea.library.LibrarySourceRefreshCoordinator
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class JvmDowngraderProjectConfigurable(private val project: Project) : Configurable {
    private val projectSettings = project.service<JvmDowngraderProjectSettings>()
    private val globalSettings = service<JvmDowngraderApplicationSettings>()
    private val globalModeBox = ComboBox(LanguageLevelMode.entries.filter { it != LanguageLevelMode.INHERIT_GLOBAL }.toTypedArray())
    private val projectModeBox = ComboBox(LanguageLevelMode.entries.toTypedArray())

    override fun getDisplayName(): String = "JVM Downgrader"

    override fun createComponent(): JComponent {
        val renderer = SimpleListCellRenderer.create<LanguageLevelMode>("") { it?.presentableName }
        globalModeBox.renderer = renderer
        projectModeBox.renderer = renderer
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Global default library source language level", globalModeBox)
            .addLabeledComponent("Project default library source language level", projectModeBox)
            .panel
    }

    override fun isModified(): Boolean =
        globalModeBox.item != globalSettings.mode || projectModeBox.item != projectSettings.mode

    override fun apply() {
        globalSettings.mode = globalModeBox.item ?: LanguageLevelMode.AUTO
        projectSettings.mode = projectModeBox.item ?: LanguageLevelMode.INHERIT_GLOBAL
        project.service<LibrarySourceRefreshCoordinator>().refresh()
    }

    override fun reset() {
        globalModeBox.item = globalSettings.mode
        projectModeBox.item = projectSettings.mode
    }
}
