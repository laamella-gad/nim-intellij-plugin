package com.laamella.nim

import com.intellij.application.options.CodeStyle
import com.intellij.lang.Language
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.lineIndent.LineIndentProvider

class NimLineIndentProvider : LineIndentProvider {
    override fun isSuitableFor(language: Language?) = language == NimLanguage

    override fun getLineIndent(project: Project, editor: Editor, language: Language?, offset: Int): String {
        val doc = editor.document
        if (offset == 0) return ""
        val caretLine = doc.getLineNumber(offset)
        if (caretLine == 0) return ""
        val prevLine = caretLine - 1
        val prevText = doc.getText(TextRange(doc.getLineStartOffset(prevLine), doc.getLineEndOffset(prevLine)))
        val prevIndent = prevText.takeWhile { it == ' ' }.length
        val lastChar = stripTrailingComment(prevText).trimEnd().lastOrNull()
        val indentSize = CodeStyle.getProjectOrDefaultSettings(project).getIndentSize(NimFileType)
        val target = if (lastChar != null && nimOpensBlock(lastChar)) prevIndent + indentSize else prevIndent
        return " ".repeat(target)
    }
}

fun nimOpensBlock(lastChar: Char) =
    lastChar == ':' || lastChar == '=' || lastChar == '(' || lastChar == '[' || lastChar == '{'

fun stripTrailingComment(line: String): String {
    var inString = false
    for (i in line.indices) {
        when {
            !inString && line[i] == '"' -> inString = true
            inString && line[i] == '"' && (i == 0 || line[i - 1] != '\\') -> inString = false
            !inString && line[i] == '#' -> return line.substring(0, i)
        }
    }
    return line
}
