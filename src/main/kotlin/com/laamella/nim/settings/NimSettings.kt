package com.laamella.nim.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import java.nio.file.Path

@State(name = "NimSettings", storages = [Storage("nim-plugin.xml")])
class NimSettings : PersistentStateComponent<NimSettings> {
    var nimbleBinPath: String = Path.of(System.getProperty("user.home"), ".nimble", "bin").toString()

    fun exePath(tool: String): String =
        if (nimbleBinPath.isBlank()) tool
        else Path.of(nimbleBinPath, tool).toString()

    override fun getState(): NimSettings = this

    override fun loadState(state: NimSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): NimSettings =
            ApplicationManager.getApplication().getService(NimSettings::class.java)
    }
}
