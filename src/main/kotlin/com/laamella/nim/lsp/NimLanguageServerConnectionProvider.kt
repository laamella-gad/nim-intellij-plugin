package com.laamella.nim.lsp

import com.intellij.openapi.project.Project
import com.laamella.nim.settings.NimSettings
import com.redhat.devtools.lsp4ij.server.ProcessStreamConnectionProvider

class NimLanguageServerConnectionProvider(project: Project) : ProcessStreamConnectionProvider() {
    init {
        val path = NimSettings.getInstance().nimlangserverPath
        commands = listOf(path)
        workingDirectory = project.basePath ?: System.getProperty("user.home")
    }
}
