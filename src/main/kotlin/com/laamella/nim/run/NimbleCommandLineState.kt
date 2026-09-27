package com.laamella.nim.run

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.laamella.nim.settings.NimSettings
import java.io.File

class NimbleCommandLineState(
    environment: ExecutionEnvironment,
    private val config: NimbleRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess() = ProcessHandlerFactory.getInstance()
        .createColoredProcessHandler(buildCommandLine())
        .also { ProcessTerminatedListener.attach(it) }

    private fun buildCommandLine(): GeneralCommandLine {
        val settings = NimSettings.getInstance()
        val cmd = GeneralCommandLine(settings.nimble(), "run").also {
            if (config.binName.isNotBlank()) it.addParameter(config.binName)
        }
        cmd.withWorkDirectory(config.workingDirectory).withCharset(Charsets.UTF_8)
        if (settings.nimbleBinPath.isNotBlank()) {
            val currentPath = System.getenv("PATH") ?: ""
            cmd.withEnvironment("PATH", "${settings.nimbleBinPath}${File.pathSeparator}$currentPath")
        }
        return cmd
    }
}
