package com.laamella.nim

import com.intellij.lang.Language

object NimLanguage : Language("Nim") {
    private fun readResolve(): Any = NimLanguage
}
