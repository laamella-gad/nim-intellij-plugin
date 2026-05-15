package com.laamella.nim.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.project.Project

class NimRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {
    override fun getId() = "Nim"
    override fun createTemplateConfiguration(project: Project) = NimRunConfiguration(project, this)
}
