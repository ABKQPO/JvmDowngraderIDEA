package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode

object LibrarySourceLanguageLevelSettingsResolver {
    fun fixedLevel(projectMode: LanguageLevelMode, globalMode: LanguageLevelMode): Int? {
        val selected = if (projectMode == LanguageLevelMode.INHERIT_GLOBAL) globalMode else projectMode
        return selected.javaVersion
    }
}
