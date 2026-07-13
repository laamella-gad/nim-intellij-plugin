package com.laamella.nim.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.laamella.nim.check.NimCheckOnSave
import com.redhat.devtools.lsp4ij.LanguageServerManager
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JTextField

class NimSettingsConfigurable : Configurable {
    private var nimbleBinPath: TextFieldWithBrowseButton? = null
    private var nimlangserverExe: JTextField? = null
    private var nimbleExe: JTextField? = null
    private var nimprettyExe: JTextField? = null

    override fun getDisplayName(): String = "Nim"

    override fun createComponent(): JComponent {
        nimbleBinPath = TextFieldWithBrowseButton().also {
            it.addBrowseFolderListener(
                null,
                FileChooserDescriptor(false, true, false, false, false, false)
                    .withTitle("Select Nim Toolchain Directory")
            )
        }
        nimlangserverExe = JTextField()
        nimbleExe = JTextField()
        nimprettyExe = JTextField()

        return panel {
            row("Nim toolchain path:") { cell(nimbleBinPath!!).align(AlignX.FILL) }
            row("nimlangserver or nimlsp executable name:") { cell(nimlangserverExe!!).align(AlignX.FILL) }
            row("nimble executable name:") { cell(nimbleExe!!).align(AlignX.FILL) }
            row("nimpretty executable name:") { cell(nimprettyExe!!).align(AlignX.FILL) }
            row {
                button("Reset to Defaults") { resetToDefaults() }
            }
            row {
                @Suppress("DialogTitleCapitalization")
                button("Set to use nimlangserver") { nimlangserverExe?.text = NimSettings().nimlangserverExe }
                @Suppress("DialogTitleCapitalization")
                button("Set to use nimlsp") { nimlangserverExe?.text = if (SystemInfo.isWindows) "nimlsp.exe" else "nimlsp" }
                // Blank exe = no LSP; NimCheckOnSaveListener provides diagnostics instead.
                @Suppress("DialogTitleCapitalization")
                button("Set to use nim check on save") { nimlangserverExe?.text = "" }
            }
        }
    }

    private fun resetToDefaults() {
        val defaults = NimSettings()
        nimbleBinPath?.text = defaults.nimbleBinPath
        nimlangserverExe?.text = defaults.nimlangserverExe
        nimbleExe?.text = defaults.nimbleExe
        nimprettyExe?.text = defaults.nimprettyExe
    }

    override fun isModified(): Boolean {
        val s = NimSettings.getInstance()
        return nimbleBinPath?.text != s.nimbleBinPath
            || nimlangserverExe?.text != s.nimlangserverExe
            || nimbleExe?.text != s.nimbleExe
            || nimprettyExe?.text != s.nimprettyExe
    }

    override fun apply() {
        val s = NimSettings.getInstance()
        val wasBlank = s.nimlangserverExe.isBlank()
        s.nimbleBinPath = nimbleBinPath?.text.orEmpty()
        s.nimlangserverExe = nimlangserverExe?.text.orEmpty()
        s.nimbleExe = nimbleExe?.text.orEmpty()
        s.nimprettyExe = nimprettyExe?.text.orEmpty()
        NimCheckOnSave.resetMissingNimWarning()
        val isBlank = s.nimlangserverExe.isBlank()
        if (wasBlank != isBlank) {
            // isEnabled() only prevents new starts; a running server must be stopped explicitly.
            ProjectManager.getInstance().openProjects.forEach { project ->
                val manager = LanguageServerManager.getInstance(project)
                if (isBlank) manager.stop("nim") else manager.start("nim")
            }
        }
    }

    override fun reset() {
        val s = NimSettings.getInstance()
        nimbleBinPath?.text = s.nimbleBinPath
        nimlangserverExe?.text = s.nimlangserverExe
        nimbleExe?.text = s.nimbleExe
        nimprettyExe?.text = s.nimprettyExe
    }

    override fun disposeUIResources() {
        nimbleBinPath = null
        nimlangserverExe = null
        nimbleExe = null
        nimprettyExe = null
    }
}
