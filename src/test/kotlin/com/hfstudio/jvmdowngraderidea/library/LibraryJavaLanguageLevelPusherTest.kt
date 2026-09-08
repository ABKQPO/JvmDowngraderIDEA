package com.hfstudio.jvmdowngraderidea.library

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.pom.java.LanguageLevel
import java.lang.reflect.Proxy
import org.junit.Assert.assertNull
import org.junit.Test

class LibraryJavaLanguageLevelPusherTest {
    @Test
    fun `language level lookup does not access the project model`() {
        val project = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Project::class.java),
        ) { _, method, _ ->
            throw AssertionError("lookup must not call Project.${method.name}")
        } as Project

        val lookup = LibraryJavaLanguageLevelPusher().getImmediateValue(
            project,
            LightVirtualFile("Dependency.java"),
        )

        assertNull(lookup)
    }

    @Test
    fun `property change callback does not access project services`() {
        val project = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Project::class.java),
        ) { _, method, _ ->
            throw AssertionError("propertyChanged must not call Project.${method.name}")
        } as Project

        val pusher = LibraryJavaLanguageLevelPusher()
        val propertyChanged = pusher.javaClass.getDeclaredMethod(
            "propertyChanged",
            Project::class.java,
            VirtualFile::class.java,
            LanguageLevel::class.java,
        )
        propertyChanged.isAccessible = true
        propertyChanged.invoke(
            pusher,
            project,
            LightVirtualFile("Dependency.java"),
            LanguageLevel.JDK_21,
        )
    }
}
