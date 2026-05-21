package com.laamella.nim.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
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
            row("nimlangserver:") { cell(nimlangserverExe!!).align(AlignX.FILL) }
            row("nimble:") { cell(nimbleExe!!).align(AlignX.FILL) }
            row("nimpretty:") { cell(nimprettyExe!!).align(AlignX.FILL) }
        }
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
        s.nimbleBinPath = nimbleBinPath?.text.orEmpty()
        s.nimlangserverExe = nimlangserverExe?.text.orEmpty()
        s.nimbleExe = nimbleExe?.text.orEmpty()
        s.nimprettyExe = nimprettyExe?.text.orEmpty()
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
