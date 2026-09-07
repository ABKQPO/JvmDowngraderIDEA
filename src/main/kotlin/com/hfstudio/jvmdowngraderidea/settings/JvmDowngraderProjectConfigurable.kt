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
    private val settings = project.service<JvmDowngraderProjectSettings>()
    private val modeBox = ComboBox(LanguageLevelMode.entries.toTypedArray())

    override fun getDisplayName(): String = "JVM Downgrader"

    override fun createComponent(): JComponent {
        modeBox.renderer = SimpleListCellRenderer.create("") { it?.presentableName }
        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Project library source language level", modeBox)
            .panel
    }

    override fun isModified(): Boolean = modeBox.item != settings.mode

    override fun apply() {
        settings.mode = modeBox.item ?: LanguageLevelMode.INHERIT_GLOBAL
        project.service<LibrarySourceRefreshCoordinator>().refresh()
    }

    override fun reset() {
        modeBox.item = settings.mode
    }
}
