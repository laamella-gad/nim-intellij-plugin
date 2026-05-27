package com.laamella.nim

import com.intellij.lexer.FlexAdapter
import com.intellij.psi.tree.IElementType

class NimLexer : FlexAdapter(_NimLexer(null))

object NimTokenTypes {
    @JvmField val LINE_COMMENT  = IElementType("NIM_LINE_COMMENT",  NimLanguage)
    @JvmField val DOC_COMMENT   = IElementType("NIM_DOC_COMMENT",   NimLanguage)
    @JvmField val BLOCK_COMMENT = IElementType("NIM_BLOCK_COMMENT", NimLanguage)
    @JvmField val KEYWORD       = IElementType("NIM_KEYWORD",       NimLanguage)
    @JvmField val STRING        = IElementType("NIM_STRING",        NimLanguage)
    @JvmField val CHAR          = IElementType("NIM_CHAR",          NimLanguage)
    @JvmField val NUMBER        = IElementType("NIM_NUMBER",        NimLanguage)
    @JvmField val IDENTIFIER    = IElementType("NIM_IDENTIFIER",    NimLanguage)
    @JvmField val OPERATOR      = IElementType("NIM_OPERATOR",      NimLanguage)
    @JvmField val LPAREN        = IElementType("NIM_LPAREN",        NimLanguage)
    @JvmField val RPAREN        = IElementType("NIM_RPAREN",        NimLanguage)
    @JvmField val LBRACKET      = IElementType("NIM_LBRACKET",      NimLanguage)
    @JvmField val RBRACKET      = IElementType("NIM_RBRACKET",      NimLanguage)
    @JvmField val LBRACE        = IElementType("NIM_LBRACE",        NimLanguage)
    @JvmField val RBRACE        = IElementType("NIM_RBRACE",        NimLanguage)
}
