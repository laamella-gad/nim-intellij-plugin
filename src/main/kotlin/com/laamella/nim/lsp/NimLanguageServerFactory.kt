package com.laamella.nim.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.laamella.nim.settings.NimSettings
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.client.features.LSPClientFeatures
import com.redhat.devtools.lsp4ij.server.OSProcessStreamConnectionProvider
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider
import java.nio.file.Path

class NimLanguageServerFactory : LanguageServerFactory {
    override fun createConnectionProvider(project: Project): StreamConnectionProvider {
        val settings = NimSettings.getInstance()
        val generalCommandLine = GeneralCommandLine(settings.nimlangserver())
            .withWorkingDirectory(Path.of(project.guessProjectDir()?.path ?: "."))
            .withEnvironment("PATH", settings.nimbleBinPath)
        return OSProcessStreamConnectionProvider(generalCommandLine)
    }

    override fun createClientFeatures(): LSPClientFeatures =
        object : LSPClientFeatures() {
            override fun isUseIntAsJsonRpcId(): Boolean = true
        }
}
