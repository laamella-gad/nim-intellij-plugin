package com.laamella.nim

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NimProjectConfiguratorTest : BasePlatformTestCase() {

    private fun sourceFolderUrls(): List<String> {
        val module = ModuleManager.getInstance(project).modules.first()
        return ModuleRootManager.getInstance(module).contentEntries
            .flatMap { it.sourceFolders.toList() }
            .map { it.url }
    }

    private fun excludedFolderUrls(): List<String> {
        val module = ModuleManager.getInstance(project).modules.first()
        return ModuleRootManager.getInstance(module).contentEntries
            .flatMap { it.excludeFolderUrls.toList() }
    }

    fun `test no nimble file is no-op`() {
        configureNimProject(project)
        // should not throw
    }

    fun `test srcDir added as source root`() {
        myFixture.addFileToProject("test.nimble", """srcDir = "src"""")
        configureNimProject(project)
        assertTrue(sourceFolderUrls().any { it.endsWith("/src") })
    }

    fun `test binDir added as excluded`() {
        myFixture.addFileToProject("test.nimble", """binDir = "bin"""")
        configureNimProject(project)
        assertTrue(excludedFolderUrls().any { it.endsWith("/bin") })
    }

    fun `test both srcDir and binDir`() {
        myFixture.addFileToProject("test.nimble", """
            srcDir = "src"
            binDir = "bin"
        """.trimIndent())
        configureNimProject(project)
        assertTrue(sourceFolderUrls().any { it.endsWith("/src") })
        assertTrue(excludedFolderUrls().any { it.endsWith("/bin") })
    }

}
