package com.laamella.nim.lsp

import com.intellij.openapi.progress.ProgressIndicator
import com.laamella.nim.settings.NimSettings
import com.redhat.devtools.lsp4ij.LanguageServersRegistry
import com.redhat.devtools.lsp4ij.installation.LanguageServerInstallerBase
import com.redhat.devtools.lsp4ij.server.definition.LanguageServerDefinition
import java.io.File

/**
 * Checks whether nimlangserver is installed and installs it via `nimble install` if not.
 * PATH is augmented with [NimSettings.nimbleBinPath] so that `nim` is findable even when
 * IntelliJ was launched without the Nim toolchain on PATH.
 */
class NimLanguageServerInstaller(
    serverDefinition: LanguageServerDefinition? = LanguageServersRegistry.getInstance().getServerDefinition("nim")
) : LanguageServerInstallerBase(serverDefinition) {

    override fun checkServerInstalled(indicator: ProgressIndicator): Boolean {
        progressCheckingServerInstalled(indicator)
        val settings = NimSettings.getInstance()
        // Blank exe = "nim check on save" mode: nothing to install.
        if (settings.nimlangserverExe.isBlank()) return true
        val exe = settings.nimlangserver()
        val file = File(exe)
        return if (file.isAbsolute) {
            file.canExecute()
        } else {
            searchOnPath(exe, settings.nimbleBinPath) != null
        }
    }

    override fun install(indicator: ProgressIndicator) {
        progressInstallingServer(indicator)
        val settings = NimSettings.getInstance()
        val nimble = settings.nimble()
        progress("Running $nimble install nimlangserver...", 0.3, indicator)
        val pb = ProcessBuilder(nimble, "install", "--accept", "nimlangserver")
            .redirectErrorStream(true)
        if (settings.nimbleBinPath.isNotBlank()) {
            val currentPath = System.getenv("PATH") ?: ""
            pb.environment()["PATH"] = "${settings.nimbleBinPath}${File.pathSeparator}$currentPath"
        }
        val process = pb.start()
        process.inputStream.bufferedReader().forEachLine { line ->
            consoleProviders.forEach { it.printProgress(line) }
        }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("$nimble install nimlangserver failed (exit $exitCode)")
        }
    }

    private fun searchOnPath(name: String, extraDir: String): File? {
        val dirs = buildList {
            if (extraDir.isNotBlank()) add(extraDir)
            addAll((System.getenv("PATH") ?: "").split(File.pathSeparator))
        }
        return dirs.map { File(it, name) }.firstOrNull { it.canExecute() }
    }
}
