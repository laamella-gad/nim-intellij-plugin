package com.laamella.nim.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import org.jdom.Element

class NimTestRunConfiguration(project: Project, factory: ConfigurationFactory) :
    LocatableConfigurationBase<RunConfigurationOptions>(project, factory, "Nim Test") {

    var workingDirectory: String = project.basePath ?: ""

    override fun getConfigurationEditor() = NimTestRunConfigurationEditor()

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        NimTestCommandLineState(environment, this)

    override fun checkConfiguration() {
        if (workingDirectory.isBlank()) throw RuntimeConfigurationError("Working directory not set")
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        workingDirectory = element.getAttributeValue("workingDirectory") ?: project.basePath ?: ""
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.setAttribute("workingDirectory", workingDirectory)
    }
}
