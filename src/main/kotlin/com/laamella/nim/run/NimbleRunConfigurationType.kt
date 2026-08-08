package com.laamella.nim.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.laamella.nim.NimIcons
import javax.swing.Icon

class NimbleRunConfigurationType : ConfigurationType {
    override fun getDisplayName() = "Nim"
    override fun getConfigurationTypeDescription() = "Run a Nim program via nimble run"
    override fun getIcon(): Icon = NimIcons.FILE
    override fun getId() = "NimRunConfiguration"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(NimbleRunConfigurationFactory(this))
}
