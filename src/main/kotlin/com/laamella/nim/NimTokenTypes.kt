package com.laamella.nim

import com.intellij.psi.tree.IElementType

object NimTokenTypes {
    val LINE_COMMENT  = IElementType("NIM_LINE_COMMENT",  NimLanguage)
    val DOC_COMMENT   = IElementType("NIM_DOC_COMMENT",   NimLanguage)
    val BLOCK_COMMENT = IElementType("NIM_BLOCK_COMMENT", NimLanguage)
    val KEYWORD       = IElementType("NIM_KEYWORD",       NimLanguage)
    val STRING        = IElementType("NIM_STRING",        NimLanguage)
    val CHAR          = IElementType("NIM_CHAR",          NimLanguage)
    val NUMBER        = IElementType("NIM_NUMBER",        NimLanguage)
    val IDENTIFIER    = IElementType("NIM_IDENTIFIER",    NimLanguage)
    val OPERATOR      = IElementType("NIM_OPERATOR",      NimLanguage)
    val LPAREN        = IElementType("NIM_LPAREN",        NimLanguage)
    val RPAREN        = IElementType("NIM_RPAREN",        NimLanguage)
    val LBRACKET      = IElementType("NIM_LBRACKET",      NimLanguage)
    val RBRACKET      = IElementType("NIM_RBRACKET",      NimLanguage)
    val LBRACE        = IElementType("NIM_LBRACE",        NimLanguage)
    val RBRACE        = IElementType("NIM_RBRACE",        NimLanguage)
}
