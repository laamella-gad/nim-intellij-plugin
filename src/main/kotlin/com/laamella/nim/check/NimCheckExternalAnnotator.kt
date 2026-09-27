package com.laamella.nim.check

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiFile
import com.laamella.nim.settings.NimSettings

/**
 * Turns [NimCheckOnSave]'s cached results into real editor annotations (via [AnnotationHolder]),
 * so they show up in the Problems tool window, gutter, and error stripe like any other inspection —
 * plain [com.intellij.openapi.editor.markup.RangeHighlighter]s added outside a highlighting pass don't.
 * Checks themselves still only run on save ([NimCheckOnSaveListener]); this class just re-applies the
 * latest cached result whenever the daemon reruns (triggered via `DaemonCodeAnalyzer.restart`).
 */
class NimCheckExternalAnnotator : ExternalAnnotator<String, List<NimCheckProblem>>() {
    override fun collectInformation(file: PsiFile): String? {
        if (NimSettings.getInstance().nimlangserverExe.isNotBlank()) return null
        return file.virtualFile?.path
    }

    override fun doAnnotate(path: String): List<NimCheckProblem> = NimCheckOnSave.problemsFor(path)

    override fun apply(file: PsiFile, problems: List<NimCheckProblem>, holder: AnnotationHolder) {
        val document = file.viewProvider.document ?: return
        for (problem in problems) {
            val range = problemRange(document, problem) ?: continue
            val severity = when (problem.severity) {
                NimCheckSeverity.ERROR -> HighlightSeverity.ERROR
                NimCheckSeverity.WARNING -> HighlightSeverity.WARNING
                NimCheckSeverity.HINT -> HighlightSeverity.WEAK_WARNING
            }
            holder.newAnnotation(severity, problem.message).range(range).create()
        }
    }
}
