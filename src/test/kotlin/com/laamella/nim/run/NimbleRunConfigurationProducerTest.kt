package com.laamella.nim.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NimbleRunConfigurationProducerTest : BasePlatformTestCase() {

    private val producer = NimbleRunConfigurationProducer()

    fun `test sets up configuration for nimble file`() {
        val file = myFixture.addFileToProject("test.nimble", "version = \"1.0.0\"")
        val context = ConfigurationContext(file)

        val configuration = producer.createConfigurationFromContext(context)?.configuration as? NimbleRunConfiguration

        assertNotNull(configuration)
        assertEquals("", configuration!!.binName)
        assertEquals("nimble run", configuration.name)
    }

    fun `test rejects non-nimble file`() {
        val file = myFixture.addFileToProject("src/main.nim", "echo \"hi\"")
        val context = ConfigurationContext(file)

        assertNull(producer.createConfigurationFromContext(context))
    }

    fun `test isConfigurationFromContext matches project nimble config`() {
        val file = myFixture.addFileToProject("test.nimble", "version = \"1.0.0\"")
        val context = ConfigurationContext(file)
        val configuration = producer.createConfigurationFromContext(context)!!.configuration as NimbleRunConfiguration

        assertTrue(producer.isConfigurationFromContext(configuration, context))
    }

    fun `test isConfigurationFromContext rejects when binName set`() {
        val file = myFixture.addFileToProject("test.nimble", "version = \"1.0.0\"")
        val context = ConfigurationContext(file)
        val configuration = producer.createConfigurationFromContext(context)!!.configuration as NimbleRunConfiguration
        configuration.binName = "someBinary"

        assertFalse(producer.isConfigurationFromContext(configuration, context))
    }
}
