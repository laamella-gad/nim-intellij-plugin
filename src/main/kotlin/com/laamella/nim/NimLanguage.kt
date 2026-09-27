package com.laamella.nim

import com.intellij.lang.Language

object NimLanguage : Language("Nim") {
    // Language is Serializable; without this, deserialization would create a second instance,
    // breaking the identity LSP4IJ's languageMapping relies on.
    private fun readResolve(): Any = NimLanguage
}
