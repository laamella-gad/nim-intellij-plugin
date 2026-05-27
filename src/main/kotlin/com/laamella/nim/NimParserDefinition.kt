package com.laamella.nim

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

/**
 * Minimal parser definition — produces a flat token stream with no AST structure.
 * A real grammar-based parser is not needed because LSP4IJ handles all semantic features;
 * the PSI tree only needs to exist so the platform can attach [NimFile] and run the lexer.
 */
class NimParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(NimLanguage)
    }

    override fun createLexer(project: Project?) = NimLexer()
    override fun getCommentTokens() = TokenSet.create(NimTokenTypes.LINE_COMMENT, NimTokenTypes.DOC_COMMENT, NimTokenTypes.BLOCK_COMMENT)
    override fun getStringLiteralElements() = TokenSet.create(NimTokenTypes.STRING, NimTokenTypes.CHAR)
    override fun getFileNodeType() = FILE
    override fun createFile(viewProvider: FileViewProvider) = NimFile(viewProvider)
    override fun createElement(node: ASTNode): PsiElement = throw UnsupportedOperationException()

    override fun createParser(project: Project?) = PsiParser { _, builder ->
        val file = builder.mark()
        while (!builder.eof()) builder.advanceLexer()
        file.done(FILE)
        builder.treeBuilt
    }
}
