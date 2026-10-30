package com.hfstudio.jvmdowngraderidea.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "JvmDowngraderApplicationSettings", storages = [Storage("jvm-downgrader-idea.xml")])
class JvmDowngraderApplicationSettings : PersistentStateComponent<JvmDowngraderApplicationSettings.StoredState> {
    class StoredState {
        var languageLevelMode: String = LanguageLevelMode.AUTO.name
        var navigateVersionedClassesToSource: Boolean = true
        var selectBaseClassForLibrarySource: Boolean = true
    }

    /** Jump from META-INF/versions classes of JVM Downgrader jars to the base class source. */
    var navigateVersionedClassesToSource: Boolean
        get() = storedState.navigateVersionedClassesToSource
        set(value) {
            storedState.navigateVersionedClassesToSource = value
        }

    /** Select the base class instead of the META-INF/versions class for an open library source. */
    var selectBaseClassForLibrarySource: Boolean
        get() = storedState.selectBaseClassForLibrarySource
        set(value) {
            storedState.selectBaseClassForLibrarySource = value
        }

    private var storedState = StoredState()

    override fun getState(): StoredState = storedState

    override fun loadState(state: StoredState) {
        storedState = state
        storedState.languageLevelMode = mode.name
    }

    var mode: LanguageLevelMode
        get() = LanguageLevelMode.entries.firstOrNull { it.name == storedState.languageLevelMode }
            ?.takeUnless { it == LanguageLevelMode.INHERIT_GLOBAL }
            ?: LanguageLevelMode.AUTO
        set(value) {
            storedState.languageLevelMode = value
                .takeUnless { it == LanguageLevelMode.INHERIT_GLOBAL }
                ?.name
                ?: LanguageLevelMode.AUTO.name
        }
}
