package com.hfstudio.jvmdowngraderidea.library

import com.intellij.openapi.project.Project
import com.intellij.testFramework.LightVirtualFile
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LibrarySourceLanguageLevelOverridesTest {
    private var nextProjectId = 1
    @Test
    fun `overrides are isolated by project and file`() {
        val projectA = projectProxy()
        val projectB = projectProxy()
        val fileA = LightVirtualFile("DependencyA.java")
        val fileB = LightVirtualFile("DependencyB.java")

        LibrarySourceLanguageLevelOverrides.set(projectA, fileA, 8)
        LibrarySourceLanguageLevelOverrides.set(projectA, fileB, 17)
        LibrarySourceLanguageLevelOverrides.set(projectB, fileA, 25)

        assertEquals(8, LibrarySourceLanguageLevelOverrides.get(projectA, fileA))
        assertEquals(17, LibrarySourceLanguageLevelOverrides.get(projectA, fileB))
        assertEquals(25, LibrarySourceLanguageLevelOverrides.get(projectB, fileA))
    }

    @Test
    fun `clearing an override restores automatic lookup`() {
        val project = projectProxy()
        val file = LightVirtualFile("Dependency.java")

        LibrarySourceLanguageLevelOverrides.set(project, file, 8)
        LibrarySourceLanguageLevelOverrides.set(project, file, null)

        assertNull(LibrarySourceLanguageLevelOverrides.get(project, file))
    }

    private fun projectProxy(): Project {
        val id = nextProjectId++
        lateinit var project: Project
        project = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Project::class.java),
        ) { _, method, args ->
            when (method.name) {
                "hashCode" -> id
                "equals" -> args?.firstOrNull() === project
                else -> null
            }
        } as Project
        return project
    }
}
