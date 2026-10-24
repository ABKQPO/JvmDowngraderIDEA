package com.hfstudio.jvmdowngraderidea.library

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.WeakHashMap

/**
 * Manual language-level choices made from the status bar.
 *
 * The pusher is queried directly by IntelliJ while reparsing a file, so the
 * choice must be available outside the refresh coordinator as well. Projects
 * are used as the scope to keep identical library sources isolated between
 * open projects.
 */
internal object LibrarySourceLanguageLevelOverrides {
    private val values = WeakHashMap<Project, MutableMap<String, Int>>()

    @Synchronized
    fun set(project: Project, file: VirtualFile, level: Int?) {
        if (level == null) {
            values[project]?.remove(file.url)
            if (values[project].isNullOrEmpty()) values.remove(project)
        } else {
            values.getOrPut(project) { HashMap() }[file.url] = level
        }
    }

    @Synchronized
    fun get(project: Project, file: VirtualFile): Int? = values[project]?.get(file.url)
}
