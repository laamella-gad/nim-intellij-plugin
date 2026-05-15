package com.laamella.nim

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class NimFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, NimLanguage) {
    override fun getFileType() = NimFileType
}
