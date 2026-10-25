package com.hfstudio.jvmdowngraderidea.navigation

import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderApplicationSettings
import com.intellij.openapi.components.service
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.compiled.ClsCustomNavigationPolicy
import com.intellij.psi.impl.compiled.ClsFileImpl

/**
 * Navigates from a versioned JVM Downgrader class to the source of its base class.
 *
 * IntelliJ only looks for META-INF/versions/N/Foo.java in the source roots,
 * which JVM Downgrader source jars do not contain. Classes, members and inner
 * classes all find their source mirror through the file navigation element,
 * so handling the file is enough.
 */
class JvmDowngraderClsNavigationPolicy : ClsCustomNavigationPolicy {
    override fun getNavigationElement(clsFile: ClsFileImpl): PsiElement? {
        if (!isEnabled()) return null
        val baseFile = JvmDowngraderMultiRelease.findBaseClassFile(clsFile.virtualFile) ?: return null
        val basePsi = PsiManager.getInstance(clsFile.project).findFile(baseFile) ?: return null
        return basePsi.navigationElement.takeIf { it !== basePsi && it is PsiClassOwner && it.isValid }
    }

    companion object {
        fun isEnabled(): Boolean = service<JvmDowngraderApplicationSettings>().navigateVersionedClassesToSource
    }
}
