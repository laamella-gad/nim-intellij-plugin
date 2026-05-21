package com.laamella.nim.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class NimSettingsTest {
    private fun settings(binPath: String) = NimSettings().also { it.nimbleBinPath = binPath }

    @Test fun `exe paths combine binPath and exe name`() {
        val s = settings("/usr/local/bin")
        assertEquals("/usr/local/bin/nimlangserver", s.nimlangserver())
        assertEquals("/usr/local/bin/nimble", s.nimble())
        assertEquals("/usr/local/bin/nimpretty", s.nimpretty())
    }

    @Test fun `blank binPath returns bare exe name`() {
        val s = settings("")
        assertEquals("nimlangserver", s.nimlangserver())
        assertEquals("nimble", s.nimble())
        assertEquals("nimpretty", s.nimpretty())
    }

    @Test fun `custom exe names used in path`() {
        val s = settings("/opt/nim/bin")
        s.nimlangserverExe = "nimlsp"
        s.nimbleExe = "nimble2"
        s.nimprettyExe = "nimpretty2"
        assertEquals("/opt/nim/bin/nimlsp", s.nimlangserver())
        assertEquals("/opt/nim/bin/nimble2", s.nimble())
        assertEquals("/opt/nim/bin/nimpretty2", s.nimpretty())
    }
}
