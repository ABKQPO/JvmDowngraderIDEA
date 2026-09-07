package com.hfstudio.jvmdowngraderidea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.PROJECT)
@State(name = "JvmDowngraderProjectSettings", storages = [Storage("jvm-downgrader-idea-project.xml")])
class JvmDowngraderProjectSettings : PersistentStateComponent<JvmDowngraderProjectSettings.StoredState> {
    class StoredState {
        var languageLevelMode: String = LanguageLevelMode.INHERIT_GLOBAL.name
    }

    private var storedState = StoredState()

    override fun getState(): StoredState = storedState

    override fun loadState(state: StoredState) {
        storedState = state
        storedState.languageLevelMode = mode.name
    }

    var mode: LanguageLevelMode
        get() = LanguageLevelMode.entries.firstOrNull { it.name == storedState.languageLevelMode }
            ?: LanguageLevelMode.INHERIT_GLOBAL
        set(value) {
            storedState.languageLevelMode = value.name
        }
}
