package com.hfstudio.jvmdowngraderidea.navigation

import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderApplicationSettings
import com.intellij.ide.projectView.SelectableTreeStructureProvider
import com.intellij.openapi.components.service
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ClassesTreeStructureProvider
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiCompiledElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod

/**
 * Selects the base class instead of the META-INF/versions class for an open
 * JVM Downgrader library source ("Select Opened File" and autoscroll).
 *
 * The project view maps a library source to its compiled original, and Java
 * resolves that to the versioned class for modern language levels. The base
 * class is the one matching the source file the user is looking at.
 */
class LibrarySourceSelectInProvider(private val project: Project) : SelectableTreeStructureProvider {
    // Stateless apart from the project; reused for its top-level element lookup.
    private val classesProvider = ClassesTreeStructureProvider(project)
    override fun modify(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings?,
    ): Collection<AbstractTreeNode<*>> = children

    override fun getTopLevelElement(element: PsiElement): PsiElement? {
        if (!service<JvmDowngraderApplicationSettings>().selectBaseClassForLibrarySource) return null
        if (DumbService.isDumb(project) || element is PsiCompiledElement) return null
        val sourceFile = element.containingFile?.virtualFile ?: return null
        if (!ProjectFileIndex.getInstance(project).isInLibrarySource(sourceFile)) return null

        val topLevel = classesProvider.getTopLevelElement(element) ?: return null
        val original = topLevel.originalElement ?: return null
        val versionedFile = original.containingFile?.virtualFile ?: return null
        val baseFile = JvmDowngraderMultiRelease.findBaseClassFile(versionedFile) ?: return null
        val basePsi = PsiManager.getInstance(project).findFile(baseFile) as? PsiClassOwner ?: return null
        return findEquivalent(original, basePsi) ?: basePsi
    }

    /** Finds [element] of the versioned class file inside the base class file. */
    private fun findEquivalent(element: PsiElement, baseFile: PsiClassOwner): PsiElement? = when (element) {
        is PsiFile -> baseFile
        is PsiClass -> {
            val outer = element.containingClass
            if (outer == null) {
                baseFile.classes.firstOrNull { it.name == element.name }
            } else {
                (findEquivalent(outer, baseFile) as? PsiClass)?.findInnerClassByName(element.name, false)
            }
        }
        is PsiMethod -> element.containingClass?.let { findEquivalent(it, baseFile) as? PsiClass }
            ?.findMethodBySignature(element, false)
        is PsiField -> element.containingClass?.let { findEquivalent(it, baseFile) as? PsiClass }
            ?.findFieldByName(element.name, false)
        else -> null
    }
}
