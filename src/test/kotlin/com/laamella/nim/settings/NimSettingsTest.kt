package com.laamella.nim.settings

import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class NimSettingsTest {
    private fun settings(binPath: String) = NimSettings().also { it.nimbleBinPath = binPath }
    private val ext = if (SystemInfo.isWindows) ".exe" else ""

    @Test fun `exe paths combine binPath and exe name`() {
        val s = settings("/usr/local/bin")
        assertEquals(Path.of("/usr/local/bin", "nimlangserver$ext").toString(), s.nimlangserver())
        assertEquals(Path.of("/usr/local/bin", "nimble$ext").toString(), s.nimble())
        assertEquals(Path.of("/usr/local/bin", "nimpretty$ext").toString(), s.nimpretty())
    }

    @Test fun `blank binPath returns bare exe name`() {
        val s = settings("")
        assertEquals("nimlangserver$ext", s.nimlangserver())
        assertEquals("nimble$ext", s.nimble())
        assertEquals("nimpretty$ext", s.nimpretty())
    }

    @Test fun `custom exe names used in path`() {
        val s = settings("/opt/nim/bin")
        s.nimlangserverExe = "nimlsp"
        s.nimbleExe = "nimble2"
        s.nimprettyExe = "nimpretty2"
        assertEquals(Path.of("/opt/nim/bin", "nimlsp").toString(), s.nimlangserver())
        assertEquals(Path.of("/opt/nim/bin", "nimble2").toString(), s.nimble())
        assertEquals(Path.of("/opt/nim/bin", "nimpretty2").toString(), s.nimpretty())
    }
}
