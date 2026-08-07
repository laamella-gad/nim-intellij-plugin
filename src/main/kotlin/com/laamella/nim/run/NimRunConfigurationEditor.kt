package com.laamella.nim.run

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class NimRunConfigurationEditor : SettingsEditor<NimRunConfiguration>() {
    private val workingDirectoryField = TextFieldWithBrowseButton()
    private val filePathField = TextFieldWithBrowseButton()
    private val binNameField = JBTextField()

    init {
        workingDirectoryField.addBrowseFolderListener(
            null,
            FileChooserDescriptor(false, true, false, false, false, false)
                .withTitle("Select Working Directory")
        )
        filePathField.addBrowseFolderListener(
            null,
            FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Select Nim File")
        )
    }

    override fun resetEditorFrom(config: NimRunConfiguration) {
        workingDirectoryField.text = config.workingDirectory
        filePathField.text = config.filePath
        binNameField.text = config.binName
    }

    override fun applyEditorTo(config: NimRunConfiguration) {
        config.workingDirectory = workingDirectoryField.text
        config.filePath = filePathField.text
        config.binName = binNameField.text
    }

    override fun createEditor(): JComponent = panel {
        row("Working directory:") {
            cell(workingDirectoryField).align(AlignX.FILL)
        }
        row("Nim file:") {
            cell(filePathField).align(AlignX.FILL)
                .comment("Optional — runs \"nim r &lt;file&gt;\" instead of \"nimble run\" when set")
        }
        row("Bin name:") {
            cell(binNameField).align(AlignX.FILL).comment("Optional — leave blank to run default bin (ignored when Nim file is set)")
        }
    }
}
