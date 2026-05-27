package com.laamella.nim

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.FileViewProvider
import javax.swing.Icon

class NimFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, NimLanguage) {
    override fun getFileType() = NimFileType
}

object NimFileType : LanguageFileType(NimLanguage) {
    override fun getName(): String = "Nim"
    override fun getDescription(): String = "Nim source file"
    override fun getDefaultExtension(): String = "nim"
    override fun getIcon(): Icon = NimIcons.FILE
}
