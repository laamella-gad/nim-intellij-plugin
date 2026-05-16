package com.laamella.nim

import com.intellij.application.options.CodeStyle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.ExternalFormatProcessor
import java.io.IOException
import java.nio.file.Files

class NimFormatProcessor : ExternalFormatProcessor {
    override fun getId() = "nimpretty"

    override fun activeForFile(source: PsiFile) = source is NimFile

    override fun format(
        source: PsiFile,
        range: TextRange,
        canChangeWhiteSpacesOnly: Boolean,
        keepLineBreaks: Boolean,
        enableBulkUpdate: Boolean,
        cursorOffset: Int
    ): TextRange? {
        val original = source.text
        val tempFile = Files.createTempFile("nim_fmt_", ".nim").toFile()
        try {
            tempFile.writeText(original)
            val process = try {
                ProcessBuilder("nimpretty", tempFile.absolutePath).redirectErrorStream(true).start()
            } catch (_: IOException) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Nim")
                    .createNotification("nimpretty not found — install with: nimble install nimpretty", NotificationType.WARNING)
                    .notify(source.project)
                return null
            }
            process.waitFor()
            if (process.exitValue() != 0) return null
            val formatted = tempFile.readText()
            if (formatted == original) return range
            val document = PsiDocumentManager.getInstance(source.project).getDocument(source) ?: return null
            document.setText(formatted)
            return TextRange(0, formatted.length)
        } catch (_: Exception) {
            return null
        } finally {
            tempFile.delete()
        }
    }

    override fun indent(source: PsiFile, lineStartOffset: Int): String? {
        if (source !is NimFile) return null
        val document = PsiDocumentManager.getInstance(source.project).getDocument(source) ?: return null
        val line = document.getLineNumber(lineStartOffset)
        if (line == 0) return ""
        val prevLineText = document.charsSequence
            .subSequence(document.getLineStartOffset(line - 1), document.getLineEndOffset(line - 1))
            .toString()
        val prevIndent = prevLineText.takeWhile { it == ' ' }.length
        val lastChar = stripTrailingComment(prevLineText).trimEnd().lastOrNull()
        val indentSize = CodeStyle.getSettings(source).getIndentSize(NimFileType)
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
