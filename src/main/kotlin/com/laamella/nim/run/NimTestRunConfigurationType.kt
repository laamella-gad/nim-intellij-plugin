package com.laamella.nim.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.laamella.nim.NimIcons
import javax.swing.Icon

class NimTestRunConfigurationType : ConfigurationType {
    override fun getDisplayName() = "Nim Test"
    override fun getConfigurationTypeDescription() = "Run Nim tests via nimble"
    override fun getIcon(): Icon = NimIcons.FILE
    override fun getId() = "NimTestRunConfiguration"
    override fun getConfigurationFactories(): Array<ConfigurationFactory> =
        arrayOf(NimTestRunConfigurationFactory(this))
}
