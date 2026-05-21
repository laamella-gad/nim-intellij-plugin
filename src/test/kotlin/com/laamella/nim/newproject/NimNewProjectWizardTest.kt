package com.laamella.nim.newproject

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NimNewProjectWizardTest {
    private val dir = Files.createTempDirectory("nim-wizard-test")

    @Test fun `binary creates src and bin directories`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.BINARY)
        assertTrue(dir.resolve("src").toFile().isDirectory)
        assertTrue(dir.resolve("bin").toFile().isDirectory)
    }

    @Test fun `binary creates nimble file with correct content`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.BINARY)
        assertEquals(
            """
            # Package
            version = "0.1.0"
            author = ""
            description = "myapp"
            license = "MIT"
            srcDir = "src"
            binDir = "bin"
            bin = @["myapp"]

            # Dependencies
            requires "nim >= $DEFAULT_NIM_VERSION"
            """.trimIndent() + "\n",
            dir.resolve("myapp.nimble").toFile().readText()
        )
    }

    @Test fun `custom version author description and license appear in nimble file`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.BINARY,
            version = "1.2.3", author = "Alice", description = "My cool app", license = "Apache-2.0")
        val nimble = dir.resolve("myapp.nimble").toFile().readText()
        assertTrue(nimble.contains("version = \"1.2.3\""))
        assertTrue(nimble.contains("author = \"Alice\""))
        assertTrue(nimble.contains("description = \"My cool app\""))
        assertTrue(nimble.contains("license = \"Apache-2.0\""))
    }

    @Test fun `binary creates executable entry point`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.BINARY)
        val content = dir.resolve("src/myapp.nim").toFile().readText()
        assertTrue(content.contains("isMainModule"))
        assertTrue(content.contains("Hello, World!"))
    }

    @Test fun `library creates src but no bin directory`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.LIBRARY)
        assertTrue(dir.resolve("src").toFile().isDirectory)
        assertFalse(dir.resolve("bin").toFile().exists())
    }

    @Test fun `library creates nimble file without bin directives`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.LIBRARY)
        val nimble = dir.resolve("myapp.nimble").toFile().readText()
        assertEquals(
            """
            # Package
            version = "0.1.0"
            author = ""
            description = "myapp"
            license = "MIT"
            srcDir = "src"

            # Dependencies
            requires "nim >= $DEFAULT_NIM_VERSION"
            """.trimIndent() + "\n",
            nimble
        )
    }

    @Test fun `library creates exported proc and submodule`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.LIBRARY)
        val main = dir.resolve("src/myapp.nim").toFile().readText()
        assertTrue(main.contains("proc add*"))
        assertTrue(dir.resolve("src/myapp/submodule.nim").toFile().exists())
        val submodule = dir.resolve("src/myapp/submodule.nim").toFile().readText()
        assertTrue(submodule.contains("Submodule*"))
        assertTrue(submodule.contains("initSubmodule*"))
    }

    @Test fun `hybrid creates src and bin directories`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.HYBRID)
        assertTrue(dir.resolve("src").toFile().isDirectory)
        assertTrue(dir.resolve("bin").toFile().isDirectory)
    }

    @Test fun `hybrid creates nimble file with installExt and bin`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.HYBRID)
        val nimble = dir.resolve("myapp.nimble").toFile().readText()
        assertEquals(
            """
            # Package
            version = "0.1.0"
            author = ""
            description = "myapp"
            license = "MIT"
            srcDir = "src"
            installExt = @["nim"]
            binDir = "bin"
            bin = @["myapp"]

            # Dependencies
            requires "nim >= $DEFAULT_NIM_VERSION"
            """.trimIndent() + "\n",
            nimble
        )
    }

    @Test fun `hybrid creates entry point importing submodule`() {
        createNimProjectStructure(dir, "myapp", NimPackageType.HYBRID)
        val main = dir.resolve("src/myapp.nim").toFile().readText()
        assertTrue(main.contains("import myapp/submodule"))
        assertTrue(main.contains("getWelcomeMessage"))
        val submodule = dir.resolve("src/myapp/submodule.nim").toFile().readText()
        assertTrue(submodule.contains("proc getWelcomeMessage*"))
    }

    @Test fun `project name used in generated files`() {
        createNimProjectStructure(dir, "coolproject")
        assertTrue(dir.resolve("coolproject.nimble").toFile().exists())
        assertTrue(dir.resolve("src/coolproject.nim").toFile().exists())
    }
}
