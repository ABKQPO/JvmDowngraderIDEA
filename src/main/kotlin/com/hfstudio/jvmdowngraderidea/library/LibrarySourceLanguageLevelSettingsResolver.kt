package com.hfstudio.jvmdowngraderidea.library

import com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode

object LibrarySourceLanguageLevelSettingsResolver {
    fun defaultMode(projectMode: LanguageLevelMode, globalMode: LanguageLevelMode): LanguageLevelMode =
        if (projectMode == LanguageLevelMode.INHERIT_GLOBAL) globalMode else projectMode

}
