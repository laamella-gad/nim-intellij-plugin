package com.laamella.nim.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.execution.actions.LazyRunConfigurationProducer
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.guessProjectDir
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
        return when (file.extension) {
            "nimble" -> {
                val baseDir = context.project.guessProjectDir() ?: return false
                configuration.workingDirectory = baseDir.path
                configuration.filePath = ""
                configuration.binName = ""
                configuration.name = "nimble run"
                true
            }

            "nim" -> {
                val parent = file.parent ?: return false
                configuration.workingDirectory = parent.path
                configuration.filePath = file.path
                configuration.binName = ""
                configuration.name = "nim r ${file.name}"
                true
            }

            else -> false
        }
    }

    override fun isConfigurationFromContext(
        configuration: NimRunConfiguration,
        context: ConfigurationContext
    ): Boolean {
        val file = context.location?.virtualFile ?: return false
        return when (file.extension) {
            "nimble" -> {
                val baseDir = context.project.guessProjectDir() ?: return false
                configuration.workingDirectory == baseDir.path && configuration.filePath.isBlank() && configuration.binName.isBlank()
            }

            "nim" -> configuration.filePath == file.path

            else -> false
        }
    }
}
