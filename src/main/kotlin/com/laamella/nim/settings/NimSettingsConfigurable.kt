package com.laamella.nim.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class NimSettingsConfigurable : Configurable {
    private var nimlangserverPath: TextFieldWithBrowseButton? = null
    private var nimblePath: TextFieldWithBrowseButton? = null
    private var nimprettyPath: TextFieldWithBrowseButton? = null

    override fun getDisplayName(): String = "Nim"

    override fun createComponent(): JComponent {
        val nimlangserverField = TextFieldWithBrowseButton()
        nimlangserverField.addBrowseFolderListener(
            null,
            FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Select Nim Language Server Executable")
        )
        nimlangserverPath = nimlangserverField

        val nimbleField = TextFieldWithBrowseButton()
        nimbleField.addBrowseFolderListener(
            null,
            FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Select Nimble Executable")
        )
        nimblePath = nimbleField

        val nimprettyField = TextFieldWithBrowseButton()
        nimprettyField.addBrowseFolderListener(
            null,
            FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle("Select Nimpretty Executable")
        )
        nimprettyPath = nimprettyField

        return panel {
            row("nimlangserver path:") {
                cell(nimlangserverField)
                    .align(AlignX.FILL)
                    .comment("Path to <code>nimlangserver</code> executable")
            }
            row("nimble path:") {
                cell(nimbleField)
                    .align(AlignX.FILL)
                    .comment("Path to <code>nimble</code> executable")
            }
            row("nimpretty path:") {
                cell(nimprettyField)
                    .align(AlignX.FILL)
                    .comment("Path to <code>nimpretty</code> executable")
            }
        }
    }

    override fun isModified(): Boolean {
        val settings = NimSettings.getInstance()
        return nimlangserverPath?.text != settings.nimlangserverPath
            || nimblePath?.text != settings.nimblePath
            || nimprettyPath?.text != settings.nimprettyPath
    }

    override fun apply() {
        val settings = NimSettings.getInstance()
        settings.nimlangserverPath = nimlangserverPath?.text.orEmpty()
        settings.nimblePath = nimblePath?.text.orEmpty()
        settings.nimprettyPath = nimprettyPath?.text.orEmpty()
    }

    override fun reset() {
        val settings = NimSettings.getInstance()
        nimlangserverPath?.text = settings.nimlangserverPath
        nimblePath?.text = settings.nimblePath
        nimprettyPath?.text = settings.nimprettyPath
    }

    override fun disposeUIResources() {
        nimlangserverPath = null
        nimblePath = null
        nimprettyPath = null
    }
}
