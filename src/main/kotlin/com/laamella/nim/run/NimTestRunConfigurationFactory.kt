package com.laamella.nim.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project

class NimTestRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId() = "NimTest"
    override fun createTemplateConfiguration(project: Project) = NimTestRunConfiguration(project, this)
}
