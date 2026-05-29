package com.laamella.nim.projectconfig

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

    private fun testSourceFolderUrls(): List<String> {
        val module = ModuleManager.getInstance(project).modules.first()
        return ModuleRootManager.getInstance(module).contentEntries
            .flatMap { it.sourceFolders.toList() }
            .filter { it.isTestSource }
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

    fun `test tests dir marked as test source root`() {
        myFixture.addFileToProject("tests/test_example.nim", "# test")
        myFixture.addFileToProject("test.nimble", "version = \"1.0.0\"")
        configureNimProject(project)
        assertTrue(testSourceFolderUrls().any { it.endsWith("/tests") })
    }

    fun `test srcDir with single quoted value`() {
        myFixture.addFileToProject("test.nimble", "srcDir = 'src'")
        configureNimProject(project)
        assertTrue(sourceFolderUrls().any { it.endsWith("/src") })
    }

    fun `test configuring twice does not duplicate source roots`() {
        myFixture.addFileToProject("test.nimble", """srcDir = "src"""")
        configureNimProject(project)
        val after1 = sourceFolderUrls()
        configureNimProject(project)
        assertEquals(after1, sourceFolderUrls())
    }

    fun `test nimble without directory keys does not add source roots`() {
        myFixture.addFileToProject("test.nimble", """version = "1.0.0"""")
        val before = sourceFolderUrls().size
        configureNimProject(project)
        assertEquals(before, sourceFolderUrls().size)
    }

    fun `test configureNimModule reuses existing module`() {
        val existing = ModuleManager.getInstance(project).modules.first()
        val result = configureNimModule(project)
        assertSame(existing, result)
    }

}