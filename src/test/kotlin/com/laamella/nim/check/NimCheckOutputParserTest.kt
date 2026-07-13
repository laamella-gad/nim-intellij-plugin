package com.laamella.nim.check

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NimCheckOutputParserTest {
    @Test
    fun `parses errors warnings and hints, drops noise`() {
        val output = """
            Hint: used config file '/home/danny/.choosenim/toolchains/nim-2.2.10/config/nim.cfg' [Conf]
            ......................................................................
            /project/src/bad.nim(2, 16) Error: type mismatch: got 'string' for '"s"' but expected 'int'
            /project/src/bad.nim(3, 8) Warning: imported and not used: 'strutils' [UnusedImport]
            /project/src/bad.nim(2, 7) Hint: 'x' is declared but not used [XDeclaredButNotUsed]
        """.trimIndent()

        val problems = parseNimCheckOutput(output)

        assertEquals(3, problems.size)
        assertEquals(
            NimCheckProblem("/project/src/bad.nim", 2, 16, NimCheckSeverity.ERROR,
                """type mismatch: got 'string' for '"s"' but expected 'int'"""),
            problems[0]
        )
        assertEquals(NimCheckSeverity.WARNING, problems[1].severity)
        assertEquals(3, problems[1].line)
        assertEquals(8, problems[1].col)
        assertEquals(NimCheckSeverity.HINT, problems[2].severity)
    }

    @Test
    fun `indented lines continue the previous problem's message`() {
        val output = listOf(
            "/p/a.nim(5, 3) Error: type mismatch: got <int>",
            "  but expected one of:",
            "  proc foo(s: string)",
            "/p/a.nim(9, 1) Warning: unreachable code [UnreachableCode]",
        ).joinToString("\n")

        val problems = parseNimCheckOutput(output)

        assertEquals(2, problems.size)
        assertEquals("type mismatch: got <int>\nbut expected one of:\nproc foo(s: string)", problems[0].message)
        assertEquals("unreachable code [UnreachableCode]", problems[1].message)
    }

    @Test
    fun `indented line before any problem is dropped`() {
        assertTrue(parseNimCheckOutput("  stray indented line").isEmpty())
    }

    @Test
    fun `parses windows paths with drive letters`() {
        val problems = parseNimCheckOutput("""C:\x\bad.nim(2, 16) Error: undeclared identifier: 'y'""")
        assertEquals(1, problems.size)
        assertEquals("""C:\x\bad.nim""", problems[0].filePath)
        assertEquals(2, problems[0].line)
        assertEquals(16, problems[0].col)
    }

    @Test
    fun `empty output yields no problems`() {
        assertTrue(parseNimCheckOutput("").isEmpty())
    }
}
