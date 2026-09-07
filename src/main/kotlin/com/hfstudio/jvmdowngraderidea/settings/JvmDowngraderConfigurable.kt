package com.hfstudio.jvmdowngraderidea.settings

import com.hfstudio.jvmdowngraderidea.library.LibrarySourceRefreshCoordinator
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent

class JvmDowngraderConfigurable : Configurable {
    private val settings = service<JvmDowngraderApplicationSettings>()
    private val modeBox = ComboBox(LanguageLevelMode.entries.filter { it != LanguageLevelMode.INHERIT_GLOBAL }.toTypedArray())

    override fun getDisplayName(): String = "JVM Downgrader"

    override fun createComponent(): JComponent {
        modeBox.renderer = SimpleListCellRenderer.create("") { it?.presentableName }
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Default library source language level", modeBox)
            .panel
    }

    override fun isModified(): Boolean = modeBox.item != settings.mode

    override fun apply() {
        settings.mode = modeBox.item ?: LanguageLevelMode.AUTO
        ProjectManager.getInstance().openProjects.forEach { project ->
            project.service<LibrarySourceRefreshCoordinator>().refresh()
        }
    }

    override fun reset() {
        modeBox.item = settings.mode
    }
}
