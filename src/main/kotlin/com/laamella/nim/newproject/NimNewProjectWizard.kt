package com.laamella.nim.newproject

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard
import com.intellij.openapi.project.Project
import com.laamella.nim.NimIcons
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.Icon
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class NimNewProjectWizard : LanguageGeneratorNewProjectWizard {
    override val name = "Nim"
    override val icon: Icon = NimIcons.FILE

    override fun createStep(parent: NewProjectWizardStep): NewProjectWizardStep = Step(parent)

    internal class Step(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        override fun setupProject(project: Project) {
            createNimProjectStructure(Paths.get(project.basePath ?: return), project.name)
        }
    }
}

fun createNimProjectStructure(dir: Path, name: String) {
    dir.resolve("bin").createDirectories()
    val src = dir.resolve("src").also { it.createDirectories() }
    dir.resolve("$name.nimble").writeText(
        """
        # Package
        version = "0.1.0"
        author = ""
        description = "$name"
        license = "MIT"
        binDir = "bin"
        srcDir = "src"
        bin = @["$name"]

        # Dependencies
        requires "nim >= 2.0.0"
        """.trimIndent() + "\n"
    )
    src.resolve("$name.nim").writeText("echo \"Hello, World!\"\n")
}
