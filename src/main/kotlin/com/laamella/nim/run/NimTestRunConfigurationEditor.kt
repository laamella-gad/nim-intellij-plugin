package com.laamella.nim.run

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class NimTestRunConfigurationEditor : SettingsEditor<NimTestRunConfiguration>() {
    private val workingDirectoryField = TextFieldWithBrowseButton()

    init {
        workingDirectoryField.addBrowseFolderListener(
            null,
            FileChooserDescriptor(false, true, false, false, false, false)
                .withTitle("Select Working Directory")
        )
    }

    override fun resetEditorFrom(config: NimTestRunConfiguration) {
        workingDirectoryField.text = config.workingDirectory
    }

    override fun applyEditorTo(config: NimTestRunConfiguration) {
        config.workingDirectory = workingDirectoryField.text
    }

    override fun createEditor(): JComponent = panel {
        row("Working directory:") {
            cell(workingDirectoryField).align(AlignX.FILL)
        }
    }
}
