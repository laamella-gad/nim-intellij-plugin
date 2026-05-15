package com.laamella.nim

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler

class NimQuoteHandler : SimpleTokenSetQuoteHandler(NimTokenTypes.STRING, NimTokenTypes.CHAR)
