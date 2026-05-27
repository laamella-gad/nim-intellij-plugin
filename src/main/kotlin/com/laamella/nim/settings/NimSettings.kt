package com.laamella.nim.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.xmlb.XmlSerializerUtil
import java.nio.file.Path

@State(name = "NimSettings", storages = [Storage("nim-plugin.xml")])
class NimSettings : PersistentStateComponent<NimSettings> {
    var nimbleBinPath: String = Path.of(System.getProperty("user.home"), ".nimble", "bin").toString()
    var nimlangserverExe: String = if (SystemInfo.isWindows) "nimlangserver.exe" else "nimlangserver"
    var nimbleExe: String = if (SystemInfo.isWindows) "nimble.exe" else "nimble"
    var nimprettyExe: String = if (SystemInfo.isWindows) "nimpretty.exe" else "nimpretty"
    var nimExe: String = if (SystemInfo.isWindows) "nim.exe" else "nim"

    fun exePath(exe: String): String =
        if (nimbleBinPath.isBlank()) exe
        else Path.of(nimbleBinPath, exe).toString()

    fun nimlangserver() = exePath(nimlangserverExe)
    fun nimble() = exePath(nimbleExe)
    fun nimpretty() = exePath(nimprettyExe)
    fun nim() = exePath(nimExe)

    override fun getState(): NimSettings = this

    override fun loadState(state: NimSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): NimSettings =
            ApplicationManager.getApplication().getService(NimSettings::class.java)
    }
}
