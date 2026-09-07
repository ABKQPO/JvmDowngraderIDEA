package com.hfstudio.jvmdowngraderidea.refresh

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ModernJavaStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.service<ModernJavaRefreshCoordinator>().start()
    }
}
