package com.laamella.nim.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class NimSettingsConfigurable : Configurable {
    private var nimbleBinPath: TextFieldWithBrowseButton? = null

    override fun getDisplayName(): String = "Nim"

    override fun createComponent(): JComponent {
        val nimbleBinField = TextFieldWithBrowseButton()
        nimbleBinField.addBrowseFolderListener(
            null,
            FileChooserDescriptor(false, true, false, false, false, false)
                .withTitle("Select Nimble Bin Directory")
        )
        nimbleBinPath = nimbleBinField

        return panel {
            row("Nimble bin path:") {
                cell(nimbleBinField)
                    .align(AlignX.FILL)
                    .comment("Directory containing <code>nimlangserver</code>, <code>nimble</code>, and <code>nimpretty</code> (e.g. <code>~/.nimble/bin</code>). Leave blank to use PATH.")
            }
        }
    }

    override fun isModified(): Boolean =
        nimbleBinPath?.text != NimSettings.getInstance().nimbleBinPath

    override fun apply() {
        NimSettings.getInstance().nimbleBinPath = nimbleBinPath?.text.orEmpty()
    }

    override fun reset() {
        nimbleBinPath?.text = NimSettings.getInstance().nimbleBinPath
    }

    override fun disposeUIResources() {
        nimbleBinPath = null
    }
}
