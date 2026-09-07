package com.hfstudio.jvmdowngraderidea.library

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class LibrarySourceStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<LibrarySourceRefreshCoordinator>().start()
    }
}
