package com.laamella.nim.lsp

import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.LanguageServerFactory
import com.redhat.devtools.lsp4ij.client.features.LSPClientFeatures
import com.redhat.devtools.lsp4ij.server.StreamConnectionProvider

class NimLanguageServerFactory : LanguageServerFactory {
    override fun createConnectionProvider(project: Project): StreamConnectionProvider =
        NimLanguageServerConnectionProvider(project)

    override fun createClientFeatures(): LSPClientFeatures =
        object : LSPClientFeatures() {
            override fun isUseIntAsJsonRpcId(): Boolean = true
        }
}
