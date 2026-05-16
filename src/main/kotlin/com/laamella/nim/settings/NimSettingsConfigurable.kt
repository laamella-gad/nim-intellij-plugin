package com.laamella.nim.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class NimSettingsConfigurable : Configurable {
    private var nimlangserverPath: TextFieldWithBrowseButton? = null

    override fun getDisplayName(): String = "Nim"

    override fun createComponent(): JComponent {
        val field = TextFieldWithBrowseButton()
        field.addBrowseFolderListener(
            null,
            FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Select Nim Language Server Executable")
        )
        nimlangserverPath = field

        return panel {
            row("nimlangserver path:") {
                cell(field)
                    .align(AlignX.FILL)
                    .comment("Leave blank to use <code>nimlangserver</code> from PATH")
            }
        }
    }

    override fun isModified(): Boolean =
        nimlangserverPath?.text != NimSettings.getInstance().nimlangserverPath

    override fun apply() {
        NimSettings.getInstance().nimlangserverPath = nimlangserverPath?.text.orEmpty()
    }

    override fun reset() {
        nimlangserverPath?.text = NimSettings.getInstance().nimlangserverPath
    }

    override fun disposeUIResources() {
        nimlangserverPath = null
    }
}
