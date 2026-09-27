package com.laamella.nim

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class NimLineIndentProviderTest : BasePlatformTestCase() {
    private val provider = NimLineIndentProvider()

    private fun indentAfter(code: String): String {
        myFixture.configureByText(NimFileType, code)
        val editor = myFixture.editor
        return provider.getLineIndent(project, editor, NimLanguage, editor.caretModel.offset)
    }

    fun `test plain line keeps indent`() = assertEquals("", indentAfter("foo\n<caret>"))
    fun `test indented line keeps indent`() = assertEquals("  ", indentAfter("  foo\n<caret>"))
    fun `test colon deepens indent`() = assertEquals("  ", indentAfter("proc foo():\n<caret>"))
    fun `test equals deepens indent`() = assertEquals("  ", indentAfter("let x =\n<caret>"))
    fun `test open paren deepens indent`() = assertEquals("  ", indentAfter("foo(\n<caret>"))
    fun `test already indented colon deepens further`() = assertEquals("    ", indentAfter("  if x:\n<caret>"))
    fun `test trailing comment stripped before checking`() = assertEquals("  ", indentAfter("proc foo(): # comment\n<caret>"))
    fun `test first line returns empty`() = assertEquals("", indentAfter("<caret>"))
    fun `test hash inside string not treated as comment`() = assertEquals("", indentAfter("let s = \"a#b\"\n<caret>"))
}
