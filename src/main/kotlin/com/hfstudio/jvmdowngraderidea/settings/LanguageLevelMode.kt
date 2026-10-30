package com.hfstudio.jvmdowngraderidea.settings

import com.hfstudio.jvmdowngraderidea.JvmDowngraderBundle

enum class LanguageLevelMode(val javaVersion: Int? = null) {
    INHERIT_GLOBAL,
    AUTO,
    JAVA_8(8),
    JAVA_11(11),
    JAVA_17(17),
    JAVA_21(21),
    JAVA_25(25);

    val presentableName: String
        get() = when (this) {
            INHERIT_GLOBAL -> JvmDowngraderBundle.message("language-level.inherit-global")
            AUTO -> JvmDowngraderBundle.message("language-level.auto")
            else -> JvmDowngraderBundle.message("language-level.java", javaVersion ?: "")
        }
}
