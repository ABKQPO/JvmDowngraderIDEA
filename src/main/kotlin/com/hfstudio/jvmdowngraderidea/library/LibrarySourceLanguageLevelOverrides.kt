package com.hfstudio.jvmdowngraderidea.library

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.HashMap

/**
 * Manual language-level choices made from the status bar.
 *
 * The pusher is queried directly by IntelliJ while reparsing a file, so the
 * choice must be available outside the refresh coordinator as well. Projects
 * are used as the scope to keep identical library sources isolated between
 * open projects.
 */
internal object LibrarySourceLanguageLevelOverrides {
    private val projectQueue = ReferenceQueue<Project>()
    private val values = HashMap<ProjectKey, MutableMap<String, Int>>()

    private class ProjectKey(
        project: Project,
        queue: ReferenceQueue<Project>? = null,
    ) : WeakReference<Project>(project, queue) {
        private val identityHash = System.identityHashCode(project)

        override fun hashCode(): Int = identityHash

        override fun equals(other: Any?): Boolean =
            other is ProjectKey && get() != null && get() === other.get()
    }

    private fun cleanup() {
        while (true) {
            val key = projectQueue.poll() as? ProjectKey? ?: return
            values.remove(key)
        }
    }

    @Synchronized
    fun set(project: Project, file: VirtualFile, level: Int?) {
        cleanup()
        val key = ProjectKey(project)
        if (level == null) {
            values[key]?.remove(file.url)
            if (values[key].isNullOrEmpty()) values.remove(key)
        } else {
            val storedKey = values.keys.firstOrNull { it == key } ?: ProjectKey(project, projectQueue)
            values.getOrPut(storedKey) { HashMap() }[file.url] = level
        }
    }

    @Synchronized
    fun get(project: Project, file: VirtualFile): Int? {
        cleanup()
        return values[ProjectKey(project)]?.get(file.url)
    }
}
