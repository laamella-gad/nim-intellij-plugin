package com.laamella.nim.newproject

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NimNewProjectWizardTest {
    private val dir = Files.createTempDirectory("nim-wizard-test")

    @Test fun `creates src and bin directories`() {
        createNimProjectStructure(dir, "myapp")
        assertTrue(dir.resolve("src").toFile().isDirectory)
        assertTrue(dir.resolve("bin").toFile().isDirectory)
    }

    @Test fun `creates nimble file with correct content`() {
        createNimProjectStructure(dir, "myapp")
        assertEquals(
            """
            # Package
            version = "0.1.0"
            author = ""
            description = "myapp"
            license = "MIT"
            binDir = "bin"
            srcDir = "src"
            bin = @["myapp"]

            # Dependencies
            requires "nim >= 2.0.0"
            """.trimIndent() + "\n",
            dir.resolve("myapp.nimble").toFile().readText()
        )
    }

    @Test fun `creates main nim file`() {
        createNimProjectStructure(dir, "myapp")
        val content = dir.resolve("src/myapp.nim").toFile().readText()
        assertTrue(content.contains("Hello, World!"))
    }

    @Test fun `project name used in generated files`() {
        createNimProjectStructure(dir, "coolproject")
        assertTrue(dir.resolve("coolproject.nimble").toFile().exists())
        assertTrue(dir.resolve("src/coolproject.nim").toFile().exists())
    }
}
