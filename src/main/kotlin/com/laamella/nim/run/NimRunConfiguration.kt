package com.laamella.nim.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import org.jdom.Element

class NimRunConfiguration(project: Project, factory: ConfigurationFactory) :
    LocatableConfigurationBase<RunConfigurationOptions>(project, factory, "Nim File") {

    var filePath: String = ""
    var workingDirectory: String = project.basePath ?: ""

    override fun getConfigurationEditor() = NimRunConfigurationEditor()

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
        NimCommandLineState(environment, this)

    override fun checkConfiguration() {
        if (filePath.isBlank()) throw RuntimeConfigurationError("Nim file not set")
        if (workingDirectory.isBlank()) throw RuntimeConfigurationError("Working directory not set")
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        filePath = element.getAttributeValue("filePath") ?: ""
        workingDirectory = element.getAttributeValue("workingDirectory") ?: project.basePath ?: ""
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.setAttribute("filePath", filePath)
        element.setAttribute("workingDirectory", workingDirectory)
    }
}
