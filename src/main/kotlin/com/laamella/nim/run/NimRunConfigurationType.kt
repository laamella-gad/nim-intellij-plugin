package com.laamella.nim.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.laamella.nim.NimIcons
import javax.swing.Icon

class NimRunConfigurationType : ConfigurationType {
    override fun getDisplayName() = "Nim File"
    override fun getConfigurationTypeDescription() = "Run a Nim file via nim r"
    override fun getIcon(): Icon = NimIcons.FILE
    override fun getId() = "NimFileRunConfiguration"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(NimRunConfigurationFactory(this))
}
