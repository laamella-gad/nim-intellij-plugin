package com.laamella.nim.run

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment

class NimTestCommandLineState(
    environment: ExecutionEnvironment,
    private val config: NimTestRunConfiguration
) : CommandLineState(environment) {

    override fun startProcess() = ProcessHandlerFactory.getInstance()
        .createColoredProcessHandler(
            GeneralCommandLine("nimble", "test")
                .withWorkDirectory(config.workingDirectory)
                .withCharset(Charsets.UTF_8)
        )
        .also { ProcessTerminatedListener.attach(it) }
}
