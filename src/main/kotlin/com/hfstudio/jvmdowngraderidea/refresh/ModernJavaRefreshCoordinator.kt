package com.hfstudio.jvmdowngraderidea.refresh

import com.hfstudio.jvmdowngraderidea.detection.GradleProjectModernJavaDetector
import com.hfstudio.jvmdowngraderidea.detection.ModernJavaDetection
import com.hfstudio.jvmdowngraderidea.resolution.EffectiveLanguageLevel
import com.hfstudio.jvmdowngraderidea.resolution.EffectiveLanguageLevelResolver
import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderApplicationSettings
import com.hfstudio.jvmdowngraderidea.settings.JvmDowngraderProjectSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.roots.LanguageLevelModuleExtension
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.pom.java.LanguageLevel
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class ModernJavaRefreshCoordinator(private val project: Project) {
    fun start() {
        project.messageBus.connect(project).subscribe(ModuleRootListener.TOPIC, object : ModuleRootListener {
            override fun rootsChanged(event: com.intellij.openapi.roots.ModuleRootEvent) = refresh()
        })
        project.messageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, GradlePropertiesListener(::refresh))
        refresh()
    }

    fun refresh() {
        if (project.isDisposed) return

        val snapshot = ApplicationManager.getApplication().runReadAction<RefreshSnapshot> {
            createSnapshot()
        }
        if (snapshot.modules.isEmpty()) return

        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater
            WriteAction.run<RuntimeException> {
                snapshot.modules.forEach { module ->
                    when (val effective = EffectiveLanguageLevelResolver.resolve(
                        projectMode = snapshot.projectMode,
                        globalMode = snapshot.globalMode,
                        detection = snapshot.detectionByRoot[module.rootPath] ?: return@forEach,
                        availableSdkVersions = snapshot.sdkVersions,
                    )) {
                        is EffectiveLanguageLevel.Ready -> updateLanguageLevel(module.module, effective.version)
                        is EffectiveLanguageLevel.MissingSdk -> thisLogger().warn(
                            "JVM Downgrader IDEA did not set ${module.module.name} to Java ${effective.requiredVersion}: " +
                                "no compatible configured Java SDK exists."
                        )
                        EffectiveLanguageLevel.Unmanaged -> Unit
                    }
                }
            }
        }
    }

    private fun createSnapshot(): RefreshSnapshot {
        val modulesWithRoots = ModuleManager.getInstance(project).modules.mapNotNull { module ->
            ExternalSystemApiUtil.getExternalRootProjectPath(module)
                ?.toNormalizedPathOrNull()
                ?.let { ModuleSnapshot(module, it) }
        }
        val roots = modulesWithRoots
            .map(ModuleSnapshot::rootPath)
            .distinct()
        val detections = roots.associateWith(GradleProjectModernJavaDetector::detect)
        val modules = modulesWithRoots.filter { it.rootPath in detections }
        val sdkVersions = ProjectJdkTable.getInstance(project).allJdks
            .mapNotNull { sdk -> sdk.versionString?.let(JavaSdkVersion::fromVersionString)?.maxLanguageLevel?.feature() }
            .toSet()

        return RefreshSnapshot(
            globalMode = service<JvmDowngraderApplicationSettings>().mode,
            projectMode = project.service<JvmDowngraderProjectSettings>().mode,
            sdkVersions = sdkVersions,
            detectionByRoot = detections,
            modules = modules,
        )
    }

    private fun updateLanguageLevel(module: Module, feature: Int) {
        val requested = LanguageLevel.forFeature(feature)
        val current = com.intellij.openapi.roots.ModuleRootManager.getInstance(module)
            .getModuleExtension(LanguageLevelModuleExtension::class.java)
            ?.languageLevel
        if (current == requested) return
        ModuleRootModificationUtil.updateModel(module) { model ->
            val extension = model.getModuleExtension(LanguageLevelModuleExtension::class.java) ?: return@updateModel
            if (extension.languageLevel != requested) {
                extension.languageLevel = requested
            }
        }
    }

    private fun String.toNormalizedPathOrNull(): Path? = runCatching {
        Path.of(this).toAbsolutePath().normalize()
    }.getOrNull()

    private data class RefreshSnapshot(
        val globalMode: com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode,
        val projectMode: com.hfstudio.jvmdowngraderidea.settings.LanguageLevelMode,
        val sdkVersions: Set<Int>,
        val detectionByRoot: Map<Path, ModernJavaDetection>,
        val modules: List<ModuleSnapshot>,
    )

    private data class ModuleSnapshot(val module: Module, val rootPath: Path)

    private class GradlePropertiesListener(private val refresh: () -> Unit) : BulkFileListener {
        override fun after(events: List<VFileEvent>) {
            if (events.any { event ->
                    event.path.endsWith("/gradle.properties") ||
                        event.path.endsWith("/gradle/gradle-daemon-jvm.properties")
                }) {
                refresh()
            }
        }
    }
}
