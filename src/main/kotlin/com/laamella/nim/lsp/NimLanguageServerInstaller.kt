package com.laamella.nim.lsp

import com.intellij.openapi.progress.ProgressIndicator
import com.laamella.nim.settings.NimSettings
import com.redhat.devtools.lsp4ij.installation.LanguageServerInstallerBase
import java.io.IOException

class NimLanguageServerInstaller : LanguageServerInstallerBase() {

    override fun checkServerInstalled(indicator: ProgressIndicator): Boolean {
        progressCheckingServerInstalled(indicator)
        val exe = NimSettings.getInstance().nimlangserver()
        return try {
            ProcessBuilder(exe).start().destroyForcibly()
            true
        } catch (_: IOException) {
            false
        }
    }

    override fun install(indicator: ProgressIndicator) {
        progressInstallingServer(indicator)
        val settings = NimSettings.getInstance()
        val nimble = settings.nimble()
        progress("Running $nimble install nimlangserver...", 0.3, indicator)
        val process = ProcessBuilder(nimble, "install", "--accept", "--useSystemNim", "nimlangserver")
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().forEachLine { line ->
            consoleProviders.forEach { it.printProgress(line) }
        }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("$nimble install nimlangserver failed (exit $exitCode)")
        }
    }
}
