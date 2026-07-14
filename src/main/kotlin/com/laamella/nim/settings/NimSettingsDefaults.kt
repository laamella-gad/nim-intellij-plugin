package com.laamella.nim.settings

import com.intellij.openapi.util.SystemInfo
import java.nio.file.Path

/**
 * Factory for the default values of [NimSettings]. Exe name defaults include `.exe` on Windows;
 * users can override with absolute paths.
 */
object NimSettingsDefaults {
    fun nimbleBinPath(): String = Path.of(System.getProperty("user.home"), ".nimble", "bin").toString()
    fun nimlangserverExe(): String = exeName("nimlangserver")
    fun nimlspExe(): String = exeName("nimlsp")
    fun nimbleExe(): String = exeName("nimble")
    fun nimprettyExe(): String = exeName("nimpretty")
    fun nimExe(): String = exeName("nim")

    private fun exeName(tool: String): String = if (SystemInfo.isWindows) "$tool.exe" else tool
}
