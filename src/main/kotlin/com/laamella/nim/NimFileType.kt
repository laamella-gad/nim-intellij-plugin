package com.laamella.nim

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object NimFileType : LanguageFileType(NimLanguage) {
    override fun getName(): String = "Nim"
    override fun getDescription(): String = "Nim source file"
    override fun getDefaultExtension(): String = "nim"
    override fun getIcon(): Icon = NimIcons.FILE
}
