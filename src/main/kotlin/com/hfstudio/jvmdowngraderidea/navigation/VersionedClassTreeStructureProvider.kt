package com.hfstudio.jvmdowngraderidea.navigation

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ClassTreeNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiUtilCore

/**
 * Keeps project view clicks on META-INF/versions classes on the class file.
 *
 * Tree navigation goes through the class navigation element, which
 * [JvmDowngraderClsNavigationPolicy] redirects to the base source. A class the
 * user picked from the versioned directory should open decompiled instead.
 */
class VersionedClassTreeStructureProvider : TreeStructureProvider, DumbAware {
    override fun modify(
        parent: AbstractTreeNode<*>,
        children: Collection<AbstractTreeNode<*>>,
        settings: ViewSettings?,
    ): Collection<AbstractTreeNode<*>> = children.map { child ->
        val psiClass = (child as? ClassTreeNode)?.value
        val file = psiClass?.let(PsiUtilCore::getVirtualFile)
        if (file != null && JvmDowngraderMultiRelease.isVersionedClassFile(file)) {
            VersionedClassTreeNode(child, psiClass)
        } else {
            child
        }
    }

    private class VersionedClassTreeNode(original: ClassTreeNode, psiClass: PsiClass) :
        ClassTreeNode(original.project, psiClass, original.settings, original.nestedFileNodes) {

        override fun navigate(requestFocus: Boolean) = navigate(requestFocus, false)

        override fun navigate(requestFocus: Boolean, preserveState: Boolean) {
            val project = project
            val file = virtualFile
            // Without a redirect the default navigation already stays on the class file.
            if (project != null && file != null && JvmDowngraderClsNavigationPolicy.isEnabled() &&
                JvmDowngraderMultiRelease.findBaseClassFile(file) != null
            ) {
                OpenFileDescriptor(project, file).navigate(requestFocus)
            } else {
                super.navigate(requestFocus, preserveState)
            }
        }
    }
}
