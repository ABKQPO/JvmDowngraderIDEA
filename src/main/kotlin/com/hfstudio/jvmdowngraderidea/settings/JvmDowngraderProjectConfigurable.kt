package com.hfstudio.jvmdowngraderidea.settings

import com.hfstudio.jvmdowngraderidea.JvmDowngraderBundle
import com.hfstudio.jvmdowngraderidea.library.LibrarySourceRefreshCoordinator
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class JvmDowngraderProjectConfigurable(private val project: Project) : Configurable {
    private val projectSettings = project.service<JvmDowngraderProjectSettings>()
    private val globalSettings = service<JvmDowngraderApplicationSettings>()
    private val globalModeBox = ComboBox(LanguageLevelMode.entries.filter { it != LanguageLevelMode.INHERIT_GLOBAL }.toTypedArray())
    private val projectModeBox = ComboBox(LanguageLevelMode.entries.toTypedArray())
    private val navigateToSourceBox = JBCheckBox(JvmDowngraderBundle.message("settings.prefer-source"))
    private val selectBaseClassBox = JBCheckBox(JvmDowngraderBundle.message("settings.fix-select-opened-file"))

    override fun getDisplayName(): String = JvmDowngraderBundle.message("settings.title")

    override fun createComponent(): JComponent {
        val renderer = SimpleListCellRenderer.create<LanguageLevelMode>("") { it?.presentableName }
        globalModeBox.renderer = renderer
        projectModeBox.renderer = renderer
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JvmDowngraderBundle.message("settings.global-default"), globalModeBox)
            .addLabeledComponent(JvmDowngraderBundle.message("settings.project-default"), projectModeBox)
            .addComponent(createJvmDowngraderGroup())
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    /** Options that only apply to JVM Downgrader jars. A Jabel group would sit next to it. */
    private fun createJvmDowngraderGroup(): JComponent = panel {
        group(JvmDowngraderBundle.message("settings.group.jvmdowngrader")) {
            row { cell(navigateToSourceBox).comment(JvmDowngraderBundle.message("settings.prefer-source.comment")) }
            row { cell(selectBaseClassBox).comment(JvmDowngraderBundle.message("settings.fix-select-opened-file.comment")) }
        }
    }

    override fun isModified(): Boolean =
        globalModeBox.item != globalSettings.mode ||
            projectModeBox.item != projectSettings.mode ||
            navigateToSourceBox.isSelected != globalSettings.navigateVersionedClassesToSource ||
            selectBaseClassBox.isSelected != globalSettings.selectBaseClassForLibrarySource

    override fun apply() {
        globalSettings.mode = globalModeBox.item ?: LanguageLevelMode.AUTO
        projectSettings.mode = projectModeBox.item ?: LanguageLevelMode.INHERIT_GLOBAL
        globalSettings.navigateVersionedClassesToSource = navigateToSourceBox.isSelected
        globalSettings.selectBaseClassForLibrarySource = selectBaseClassBox.isSelected
        project.service<LibrarySourceRefreshCoordinator>().refresh()
    }

    override fun reset() {
        globalModeBox.item = globalSettings.mode
        projectModeBox.item = projectSettings.mode
        navigateToSourceBox.isSelected = globalSettings.navigateVersionedClassesToSource
        selectBaseClassBox.isSelected = globalSettings.selectBaseClassForLibrarySource
    }
}
