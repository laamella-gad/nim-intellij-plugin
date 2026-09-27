package com.laamella.nim.check

import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NimCheckProblemRangeTest : BasePlatformTestCase() {
    private fun rangeAt(text: String, line: Int, col: Int): TextRange? {
        myFixture.configureByText("test.nim", text)
        return problemRange(myFixture.editor.document, problem(line, col))
    }

    private fun problem(line: Int, col: Int) =
        NimCheckProblem("test.nim", line, col, NimCheckSeverity.ERROR, "msg")

    fun testHighlightsIdentifierAtPosition() {
        assertEquals(TextRange(5, 8), rangeAt("proc foo() =\n  echo bar\n", 1, 6))
    }

    fun testHighlightsIdentifierOnSecondLine() {
        assertEquals(TextRange(20, 23), rangeAt("proc foo() =\n  echo bar\n", 2, 8))
    }

    fun testFallsBackToSingleCharAtPunctuation() {
        assertEquals(TextRange(4, 5), rangeAt("echo \"x\"\n", 1, 5))
    }

    fun testReturnsNullForLineOutOfRange() {
        assertNull(rangeAt("echo 1\n", 99, 1))
    }

    fun testClampsColumnBeyondLineToLastChar() {
        assertEquals(TextRange(1, 2), rangeAt("ab\n", 1, 99))
    }

    fun testReturnsNullForEmptyLine() {
        assertNull(rangeAt("\necho 1\n", 1, 1))
    }
}
