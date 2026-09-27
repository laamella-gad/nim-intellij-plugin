package com.laamella.nim.newproject

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.language.LanguageGeneratorNewProjectWizard
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.laamella.nim.NimIcons
import com.laamella.nim.settings.NimSettings
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.Icon
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

enum class NimPackageType(val displayName: String) {
    BINARY("Binary  - produces an executable for the end-user"),
    LIBRARY("Library - provides functionality for other packages"),
    HYBRID("Hybrid  - combination of library and binary");

    override fun toString() = displayName
}

val NIM_LICENSES = listOf(
    "MIT", "GPL-2.0", "Apache-2.0", "ISC", "GPL-3.0", "BSD-3-Clause",
    "LGPL-2.1", "LGPL-3.0", "LGPL-3.0-linking-exception",
    "EPL-2.0", "AGPL-3.0", "Proprietary", "Other"
)

const val DEFAULT_NIM_VERSION = "2.0.0"

private fun detectedNimVersion(): String {
    val nim = NimSettings.getInstance().nim()
    return try {
        val line = ProcessBuilder(nim, "--version").start()
            .inputStream.bufferedReader().readLine() ?: return DEFAULT_NIM_VERSION
        Regex("""Version (\d+\.\d+\.\d+)""").find(line)?.groupValues?.get(1) ?: DEFAULT_NIM_VERSION
    } catch (_: Exception) { DEFAULT_NIM_VERSION }
}

private fun gitConfigUser(): String = try {
    ProcessBuilder("git", "config", "user.name").start()
        .inputStream.bufferedReader().readLine() ?: ""
} catch (_: Exception) { "" }

class NimNewProjectWizard : LanguageGeneratorNewProjectWizard {
    override val name = "Nim"
    override val icon: Icon = NimIcons.FILE

    override fun createStep(parent: NewProjectWizardStep): NewProjectWizardStep = Step(parent)

    internal class Step(parent: NewProjectWizardStep) : AbstractNewProjectWizardStep(parent) {
        private val packageTypeProperty = propertyGraph.property(NimPackageType.BINARY)
        private var packageType by packageTypeProperty

        private val versionProperty = propertyGraph.property("0.1.0")
        private var version by versionProperty

        private val authorProperty = propertyGraph.property(gitConfigUser())
        private var author by authorProperty

        private val descriptionProperty = propertyGraph.property("")
        private var description by descriptionProperty

        private val licenseProperty = propertyGraph.property<String?>(NIM_LICENSES.first())
        private var license by licenseProperty

        private val nimVersionProperty = propertyGraph.property(detectedNimVersion())
        private var nimVersion by nimVersionProperty

        override fun setupUI(builder: Panel) {
            with(builder) {
                row("Package type:") {
                    comboBox(NimPackageType.entries).bindItem(packageTypeProperty)
                }
                row("Version:") {
                    textField().bindText(versionProperty)
                }
                row("Author:") {
                    textField().bindText(authorProperty)
                }
                row("Description:") {
                    textField().bindText(descriptionProperty)
                }
                row("License:") {
                    comboBox(NIM_LICENSES).bindItem(::license)
                }
            }
        }

        override fun setupProject(project: Project) {
            createNimProjectStructure(
                Paths.get(project.basePath ?: return),
                project.name, packageType, version, author, description, license ?: NIM_LICENSES.first(), nimVersion
            )
        }
    }
}

fun createNimProjectStructure(
    dir: Path,
    name: String,
    packageType: NimPackageType = NimPackageType.BINARY,
    version: String = "0.1.0",
    author: String = "",
    description: String = name,
    license: String = "MIT",
    nimVersion: String = DEFAULT_NIM_VERSION
) {
    val src = dir.resolve("src").also { it.createDirectories() }
    val hasBin = packageType != NimPackageType.LIBRARY
    val hasSubmodule = packageType != NimPackageType.BINARY
    if (hasBin) dir.resolve("bin").createDirectories()

    dir.resolve("$name.nimble").writeText(buildString {
        appendLine("# Package")
        appendLine("version = \"$version\"")
        appendLine("author = \"$author\"")
        appendLine("description = \"$description\"")
        appendLine("license = \"$license\"")
        appendLine("srcDir = \"src\"")
        if (packageType == NimPackageType.HYBRID) appendLine("installExt = @[\"nim\"]")
        if (hasBin) {
            appendLine("binDir = \"bin\"")
            appendLine("bin = @[\"$name\"]")
        }
        appendLine()
        appendLine("# Dependencies")
        appendLine("requires \"nim >= $nimVersion\"")
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
