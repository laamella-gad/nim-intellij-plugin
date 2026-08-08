package com.laamella.nim.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NimTestRunConfigurationProducerTest : BasePlatformTestCase() {

    private val producer = NimTestRunConfigurationProducer()

    fun `test sets up configuration for nimble file`() {
        val file = myFixture.addFileToProject("test.nimble", "version = \"1.0.0\"")
        val context = ConfigurationContext(file)

        val configuration = producer.createConfigurationFromContext(context)?.configuration as? NimTestRunConfiguration

        assertNotNull(configuration)
        assertEquals("nimble test", configuration!!.name)
    }

    fun `test rejects non-nimble file`() {
        val file = myFixture.addFileToProject("src/main.nim", "echo \"hi\"")
        val context = ConfigurationContext(file)

        assertNull(producer.createConfigurationFromContext(context))
    }

    fun `test isConfigurationFromContext matches project nimble config`() {
        val file = myFixture.addFileToProject("test.nimble", "version = \"1.0.0\"")
        val context = ConfigurationContext(file)
        val configuration = producer.createConfigurationFromContext(context)!!.configuration as NimTestRunConfiguration

        assertTrue(producer.isConfigurationFromContext(configuration, context))
    }
}
