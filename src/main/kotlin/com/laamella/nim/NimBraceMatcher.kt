package com.laamella.nim

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class NimBraceMatcher : PairedBraceMatcher {
    private val pairs = arrayOf(
        BracePair(NimTokenTypes.LPAREN,   NimTokenTypes.RPAREN,   false),
        BracePair(NimTokenTypes.LBRACKET, NimTokenTypes.RBRACKET, false),
        BracePair(NimTokenTypes.LBRACE,   NimTokenTypes.RBRACE,   false),
    )

    override fun getPairs() = pairs
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?) = true
    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int) = openingBraceOffset
}
