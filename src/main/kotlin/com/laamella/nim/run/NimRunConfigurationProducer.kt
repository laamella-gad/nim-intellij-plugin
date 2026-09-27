package com.laamella.nim.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiElement

class NimRunConfigurationProducer : LazyRunConfigurationProducer<NimRunConfiguration>() {

    override fun getConfigurationFactory(): ConfigurationFactory =
        NimRunConfigurationType().configurationFactories[0]

    override fun setupConfigurationFromContext(
        configuration: NimRunConfiguration,
        context: ConfigurationContext,
        sourceElement: Ref<PsiElement>
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        if (file.extension != "nim") return false
        val parent = file.parent ?: return false
        configuration.workingDirectory = parent.path
        configuration.filePath = file.path
        configuration.name = "nim r ${file.name}"
        return true
    }

    override fun isConfigurationFromContext(
        configuration: NimRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        if (file.extension != "nim") return false
        return configuration.filePath == file.path
    }
}
