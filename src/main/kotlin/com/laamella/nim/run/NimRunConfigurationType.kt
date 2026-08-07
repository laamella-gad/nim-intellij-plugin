package com.laamella.nim.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.laamella.nim.NimIcons
import javax.swing.Icon

class NimRunConfigurationType : ConfigurationType {
    override fun getDisplayName() = "Nim"
    override fun getConfigurationTypeDescription() = "Run a Nim program via nimble run or nim r"
    override fun getIcon(): Icon = NimIcons.FILE
    override fun getId() = "NimRunConfiguration"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(NimRunConfigurationFactory(this))
}
