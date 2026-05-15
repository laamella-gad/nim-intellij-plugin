package com.laamella.nim.run

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment

class NimCommandLineState(
    environment: ExecutionEnvironment,
    private val config: NimRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess() = ProcessHandlerFactory.getInstance()
        .createColoredProcessHandler(buildCommandLine())
        .also { ProcessTerminatedListener.attach(it) }

    private fun buildCommandLine(): GeneralCommandLine {
        val cmd = GeneralCommandLine("nimble", "run")
            .withWorkDirectory(config.workingDirectory)
            .withCharset(Charsets.UTF_8)
        if (config.binName.isNotBlank()) cmd.addParameter(config.binName)
        return cmd
    }
}
