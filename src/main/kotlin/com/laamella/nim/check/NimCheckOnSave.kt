package com.laamella.nim.check

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.laamella.nim.settings.NimSettings
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

enum class NimCheckSeverity { ERROR, WARNING, HINT }

data class NimCheckProblem(
    val filePath: String,
    val line: Int,   // 1-based
    val col: Int,    // 1-based
    val severity: NimCheckSeverity,
    val message: String,
)

private val PROBLEM_LINE = Regex("""^(.+?)\((\d+), (\d+)\) (Error|Warning|Hint): (.*)$""")

/**
 * Parses `nim check` output into problems. Lines that don't carry a position
 * (config hints, dot progress lines) are dropped; indented lines continue the
 * previous problem's message (e.g. type mismatch candidate lists).
 */
internal fun parseNimCheckOutput(output: String): List<NimCheckProblem> {
    val problems = mutableListOf<NimCheckProblem>()
    for (line in output.lineSequence()) {
        val m = PROBLEM_LINE.matchEntire(line)
        if (m != null) {
            val (path, ln, col, sev, msg) = m.destructured
            problems += NimCheckProblem(path, ln.toInt(), col.toInt(), NimCheckSeverity.valueOf(sev.uppercase()), msg)
        } else if (problems.isNotEmpty() && line.isNotBlank() && line.first().isWhitespace()) {
            val last = problems.removeLast()
            problems += last.copy(message = last.message + "\n" + line.trim())
        }
    }
    return problems
}

/** Text range to underline for a problem: the identifier at (line, col), or a single character. */
internal fun problemRange(document: Document, problem: NimCheckProblem): TextRange? {
    if (problem.line < 1 || problem.line > document.lineCount) return null
    val lineStart = document.getLineStartOffset(problem.line - 1)
    val lineEnd = document.getLineEndOffset(problem.line - 1)
    if (lineStart == lineEnd) return null // empty line
    val start = (lineStart + problem.col - 1).coerceIn(lineStart, lineEnd - 1)
    val text = document.charsSequence
    var end = start
    while (end < lineEnd && (text[end].isLetterOrDigit() || text[end] == '_')) end++
    if (end == start) end = start + 1
    return TextRange(start, end)
}

/**
 * Runs `nim check` on every saved `.nim` file and marks the reported problems in the editor.
 * Active only when no language server executable is configured ([NimSettings.nimlangserverExe]
 * blank) — otherwise diagnostics come from the LSP server.
 */
class NimCheckOnSaveListener(private val project: Project) : BulkFileListener {
    override fun after(events: List<VFileEvent>) {
        if (NimSettings.getInstance().nimlangserverExe.isNotBlank()) return
        events.asSequence()
            .filter { it is VFileContentChangeEvent && it.isFromSave }
            .mapNotNull { it.file }
            .filter { it.extension == "nim" }
            .distinctBy { it.path }
            .forEach { NimCheckOnSave.runNimCheck(project, it) }
    }
}

object NimCheckOnSave {
    private val HIGHLIGHTERS_KEY = Key.create<List<RangeHighlighter>>("nim.check.highlighters")
    private val runningChecks = ConcurrentHashMap<String, Process>()
    private val warnedMissingNim = AtomicBoolean(false)

    /** Re-arms the "nim not found" balloon, e.g. after the user fixed the toolchain path. */
    fun resetMissingNimWarning() = warnedMissingNim.set(false)

    fun runNimCheck(project: Project, file: VirtualFile) {
        val settings = NimSettings.getInstance()
        ApplicationManager.getApplication().executeOnPooledThread {
            runningChecks.remove(file.path)?.destroy()
            val pb = ProcessBuilder(settings.nim(), "check", file.path)
                .directory(File(project.guessProjectDir()?.path ?: "."))
                .redirectErrorStream(true)
            if (settings.nimbleBinPath.isNotBlank()) {
                val currentPath = System.getenv("PATH") ?: ""
                pb.environment()["PATH"] = "${settings.nimbleBinPath}${File.pathSeparator}$currentPath"
            }
            val process = try {
                pb.start()
            } catch (_: IOException) {
                if (warnedMissingNim.compareAndSet(false, true)) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Nim")
                        .createNotification(
                            "nim not found — check the Nim toolchain path in Settings → Languages & Frameworks → Nim",
                            NotificationType.WARNING
                        )
                        .notify(project)
                }
                return@executeOnPooledThread
            }
            runningChecks[file.path] = process
            val output = process.inputStream.bufferedReader().readText() // non-zero exit just means problems found
            process.waitFor()
            if (runningChecks.remove(file.path, process)) {
                val wanted = Path.of(file.path).normalize()
                val problems = parseNimCheckOutput(output)
                    .filter { runCatching { Path.of(it.filePath).normalize() == wanted }.getOrDefault(false) }
                ApplicationManager.getApplication().invokeLater { applyProblems(project, file, problems) }
            }
        }
    }

    private fun applyProblems(project: Project, file: VirtualFile, problems: List<NimCheckProblem>) {
        if (project.isDisposed || !file.isValid) return
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return
        val markup = DocumentMarkupModel.forDocument(document, project, true)

        document.getUserData(HIGHLIGHTERS_KEY)?.forEach { it.dispose() }

        val scheme = EditorColorsManager.getInstance().globalScheme
        val highlighters = problems.mapNotNull { problem ->
            val range = problemRange(document, problem) ?: return@mapNotNull null
            val (layer, attributesKey) = when (problem.severity) {
                NimCheckSeverity.ERROR -> HighlighterLayer.ERROR to CodeInsightColors.ERRORS_ATTRIBUTES
                NimCheckSeverity.WARNING -> HighlighterLayer.WARNING to CodeInsightColors.WARNINGS_ATTRIBUTES
                NimCheckSeverity.HINT -> HighlighterLayer.WEAK_WARNING to CodeInsightColors.WEAK_WARNING_ATTRIBUTES
            }
            markup.addRangeHighlighter(
                range.startOffset, range.endOffset, layer,
                scheme.getAttributes(attributesKey), HighlighterTargetArea.EXACT_RANGE
            ).apply {
                errorStripeTooltip = "nim check: ${problem.message}"
            }
        }
        document.putUserData(HIGHLIGHTERS_KEY, highlighters)
    }
}
