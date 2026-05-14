package com.laamella.nim.lsp

import com.laamella.nim.settings.NimSettings
import com.intellij.openapi.project.Project
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider

class NimLanguageServerConnectionProvider(project: Project) : ProcessStreamConnectionProvider() {
    init {
        val path = NimSettings.getInstance().serverPath.ifBlank { "nimlangserver" }
        commands = listOf(path)
        workingDirectory = project.basePath
    }
}
