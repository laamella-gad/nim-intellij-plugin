package com.laamella.nim

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

object NimHighlightingColors {
    val LINE_COMMENT  = key("NIM_LINE_COMMENT",  DefaultLanguageHighlighterColors.LINE_COMMENT)
    val DOC_COMMENT   = key("NIM_DOC_COMMENT",   DefaultLanguageHighlighterColors.DOC_COMMENT)
    val BLOCK_COMMENT = key("NIM_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
    val KEYWORD       = key("NIM_KEYWORD",       DefaultLanguageHighlighterColors.KEYWORD)
    val STRING        = key("NIM_STRING",        DefaultLanguageHighlighterColors.STRING)
    val NUMBER        = key("NIM_NUMBER",        DefaultLanguageHighlighterColors.NUMBER)
    val IDENTIFIER    = key("NIM_IDENTIFIER",    DefaultLanguageHighlighterColors.IDENTIFIER)
    val OPERATOR      = key("NIM_OPERATOR",      DefaultLanguageHighlighterColors.OPERATION_SIGN)
    val PAREN         = key("NIM_PAREN",         DefaultLanguageHighlighterColors.PARENTHESES)
    val BRACKET       = key("NIM_BRACKET",       DefaultLanguageHighlighterColors.BRACKETS)
    val BRACE         = key("NIM_BRACE",         DefaultLanguageHighlighterColors.BRACES)

    private fun key(name: String, fallback: TextAttributesKey) =
        TextAttributesKey.createTextAttributesKey(name, fallback)
}

class NimSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = NimLexer()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = when (tokenType) {
        NimTokenTypes.LINE_COMMENT            -> arrayOf(NimHighlightingColors.LINE_COMMENT)
        NimTokenTypes.DOC_COMMENT             -> arrayOf(NimHighlightingColors.DOC_COMMENT)
        NimTokenTypes.BLOCK_COMMENT           -> arrayOf(NimHighlightingColors.BLOCK_COMMENT)
        NimTokenTypes.KEYWORD                 -> arrayOf(NimHighlightingColors.KEYWORD)
        NimTokenTypes.STRING, NimTokenTypes.CHAR -> arrayOf(NimHighlightingColors.STRING)
        NimTokenTypes.NUMBER                  -> arrayOf(NimHighlightingColors.NUMBER)
        NimTokenTypes.IDENTIFIER              -> arrayOf(NimHighlightingColors.IDENTIFIER)
        NimTokenTypes.OPERATOR                -> arrayOf(NimHighlightingColors.OPERATOR)
        NimTokenTypes.LPAREN, NimTokenTypes.RPAREN     -> arrayOf(NimHighlightingColors.PAREN)
        NimTokenTypes.LBRACKET, NimTokenTypes.RBRACKET -> arrayOf(NimHighlightingColors.BRACKET)
        NimTokenTypes.LBRACE, NimTokenTypes.RBRACE    -> arrayOf(NimHighlightingColors.BRACE)
        else                                  -> emptyArray()
    }
}
