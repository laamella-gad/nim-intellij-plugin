package com.laamella.nim.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class NimbleRunConfigurationProducer : LazyRunConfigurationProducer<NimbleRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        NimbleRunConfigurationType().configurationFactories[0]

    override fun setupConfigurationFromContext(
        configuration: NimbleRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        if (file.extension != "nimble") return false
        val baseDir = context.project.guessProjectDir() ?: return false
        configuration.workingDirectory = baseDir.path
        configuration.binName = ""
        configuration.name = "nimble run"
        return true
    }

    override fun isConfigurationFromContext(
        configuration: NimbleRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        if (file.extension != "nimble") return false
        val baseDir = context.project.guessProjectDir() ?: return false
        return configuration.workingDirectory == baseDir.path && configuration.binName.isBlank()
    }
}
