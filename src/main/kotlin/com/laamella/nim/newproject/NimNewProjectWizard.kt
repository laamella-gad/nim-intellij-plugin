package com.laamella.nim.newproject

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.laamella.nim.NimIcons
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.Icon
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

enum class NimPackageType(val displayName: String) {
    BINARY("Binary  - produces an executable for the end-user."),
    LIBRARY("Library - provides functionality for other packages."),
    HYBRID("Hybrid  - combination of library and binary");

    override fun toString() = displayName
}

class NimNewProjectWizard : LanguageGeneratorNewProjectWizard {
    override val name = "Nim"
    override val icon: Icon = NimIcons.FILE

    override fun createStep(parent: NewProjectWizardStep): NewProjectWizardStep = Step(parent)

    internal class Step(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        private val packageTypeProperty = propertyGraph.property(NimPackageType.BINARY)
        private var packageType by packageTypeProperty

        override fun setupUI(builder: Panel) {
            with(builder) {
                row("Package type:") {
                    comboBox(NimPackageType.entries).bindItem(packageTypeProperty)
                }
            }
        }

        override fun setupProject(project: Project) {
            createNimProjectStructure(Paths.get(project.basePath ?: return), project.name, packageType)
        }
    }
}

fun createNimProjectStructure(dir: Path, name: String, packageType: NimPackageType = NimPackageType.BINARY) {
    val src = dir.resolve("src").also { it.createDirectories() }
    val hasBin = packageType != NimPackageType.LIBRARY
    val hasSubmodule = packageType != NimPackageType.BINARY
    if (hasBin) dir.resolve("bin").createDirectories()

    dir.resolve("$name.nimble").writeText(buildString {
        appendLine("# Package")
        appendLine("version = \"0.1.0\"")
        appendLine("author = \"\"")
        appendLine("description = \"$name\"")
        appendLine("license = \"MIT\"")
        appendLine("srcDir = \"src\"")
        if (packageType == NimPackageType.HYBRID) appendLine("installExt = @[\"nim\"]")
        if (hasBin) {
            appendLine("binDir = \"bin\"")
            appendLine("bin = @[\"$name\"]")
        }
        appendLine()
        appendLine("# Dependencies")
        appendLine("requires \"nim >= 2.0.0\"")
    })

    val mainSource = when (packageType) {
        NimPackageType.BINARY -> """
            # This is just an example to get you started. A typical binary package
            # uses this file as the main entry point of the application.

            when isMainModule:
              echo("Hello, World!")
            """.trimIndent() + "\n"
        NimPackageType.LIBRARY -> """
            # This is just an example to get you started. A typical library package
            # exports the main API in this file. Note that you cannot rename this file
            # but you can remove it if you wish.

            proc add*(x, y: int): int =
              ## Adds two numbers together.
              return x + y
            """.trimIndent() + "\n"
        NimPackageType.HYBRID -> """
            # This is just an example to get you started. A typical hybrid package
            # uses this file as the main entry point of the application.

            import $name/submodule

            when isMainModule:
              echo(getWelcomeMessage())
            """.trimIndent() + "\n"
    }
    src.resolve("$name.nim").writeText(mainSource)

    if (hasSubmodule) {
        val submoduleDir = src.resolve(name).also { it.createDirectories() }
        val submoduleSource = if (packageType == NimPackageType.LIBRARY) """
                # This is just an example to get you started. Users of your library will
                # import this file by writing ``import $name/submodule``. Feel free to rename or
                # remove this file altogether. You may create additional modules alongside
                # this file as required.

                type
                  Submodule* = object
                    name*: string

                proc initSubmodule*(): Submodule =
                  ## Initialises a new ``Submodule`` object.
                  Submodule(name: "Anonymous")
                """.trimIndent() + "\n"
            else """
                # This is just an example to get you started. Users of your hybrid library
                # will import this file by writing ``import $name/submodule``. Feel free to
                # rename or remove this file altogether. You may create additional modules
                # alongside this file as required.

                proc getWelcomeMessage*(): string = "Hello, World!"
                """.trimIndent() + "\n"
        submoduleDir.resolve("submodule.nim").writeText(submoduleSource)
    }
}
