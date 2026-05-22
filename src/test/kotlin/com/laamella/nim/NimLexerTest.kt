package com.laamella.nim

import com.intellij.lexer.Lexer
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import kotlin.test.Test
import kotlin.test.assertEquals

class NimLexerTest {
    private fun tokenize(input: String): List<Pair<IElementType?, String>> {
        val lexer: Lexer = NimLexer()
        lexer.start(input)
        val result = mutableListOf<Pair<IElementType?, String>>()
        while (lexer.tokenType != null) {
            result += lexer.tokenType to lexer.tokenText
            lexer.advance()
        }
        return result
    }

    private fun types(input: String) = tokenize(input).map { it.first }

    // Asserts exactly one token and returns its type.
    private fun only(input: String): IElementType? {
        val tokens = tokenize(input)
        assertEquals(1, tokens.size, "Expected exactly 1 token for input: $input, got: $tokens")
        return tokens[0].first
    }

    // Returns the first token type.
    private fun first(input: String) = tokenize(input).first().first

    // --- Comments ---

    @Test fun `line comment`() = assertEquals(NimTokenTypes.LINE_COMMENT, only("# hello world"))
    @Test fun `line comment empty`() = assertEquals(NimTokenTypes.LINE_COMMENT, only("#"))
    @Test fun `doc line comment`() = assertEquals(NimTokenTypes.DOC_COMMENT, only("## docs"))
    @Test fun `block comment`() = assertEquals(NimTokenTypes.BLOCK_COMMENT, only("#[ block ]#"))
    @Test fun `nested block comment`() = assertEquals(NimTokenTypes.BLOCK_COMMENT, only("#[ outer #[ inner ]# outer ]#"))
    @Test fun `doc block comment`() = assertEquals(NimTokenTypes.DOC_COMMENT, only("##[ doc block ]##"))
    @Test fun `nested doc block comment`() = assertEquals(NimTokenTypes.DOC_COMMENT, only("##[ outer ##[ inner ]## outer ]##"))

    // --- Keywords ---
    // Keywords require a non-ident lookahead; test with trailing space.

    @Test fun `keyword proc`() = assertEquals(NimTokenTypes.KEYWORD, first("proc "))
    @Test fun `keyword var`() = assertEquals(NimTokenTypes.KEYWORD, first("var "))
    @Test fun `keyword let`() = assertEquals(NimTokenTypes.KEYWORD, first("let "))
    @Test fun `keyword if`() = assertEquals(NimTokenTypes.KEYWORD, first("if "))
    @Test fun `keyword nil`() = assertEquals(NimTokenTypes.KEYWORD, first("nil "))
    @Test fun `keyword not`() = assertEquals(NimTokenTypes.KEYWORD, first("not "))
    @Test fun `keyword type`() = assertEquals(NimTokenTypes.KEYWORD, first("type "))
    @Test fun `keyword followed by digit is identifier`() = assertEquals(NimTokenTypes.IDENTIFIER, only("proc1"))
    @Test fun `keyword followed by letter is identifier`() = assertEquals(NimTokenTypes.IDENTIFIER, only("procedure"))
    @Test fun `keyword preceding paren is keyword`() = assertEquals(NimTokenTypes.KEYWORD, first("if("))

    // --- Strings ---

    @Test fun `normal string`() = assertEquals(NimTokenTypes.STRING, only("\"hello\""))
    @Test fun `empty string`() = assertEquals(NimTokenTypes.STRING, only("\"\""))
    @Test fun `string with backslash escape`() = assertEquals(NimTokenTypes.STRING, only("\"a\\nb\""))
    @Test fun `triple string`() = assertEquals(NimTokenTypes.STRING, only("\"\"\"hello\"\"\""))
    @Test fun `triple string with embedded double quote`() = assertEquals(NimTokenTypes.STRING, only("\"\"\"a \"b\" c\"\"\""))
    @Test fun `raw string`() = assertEquals(NimTokenTypes.STRING, only("r\"hello\""))
    @Test fun `raw string uppercase R`() = assertEquals(NimTokenTypes.STRING, only("R\"world\""))
    @Test fun `raw string doubled quote`() = assertEquals(NimTokenTypes.STRING, only("r\"say \"\"hi\"\"\""))
    @Test fun `triple raw string`() = assertEquals(NimTokenTypes.STRING, only("r\"\"\"hello\"\"\""))
    @Test fun `generalized string`() = assertEquals(NimTokenTypes.STRING, only("fmt\"hello {name}\""))
    @Test fun `generalized triple string`() {
        // fmt"""...""" — generalized triple string; lexer enters IN_TRIPLE_STRING on the """
        assertEquals(NimTokenTypes.STRING, only("fmt\"\"\"hello\"\"\""))
    }

    // --- Char literals ---

    @Test fun `char literal`() = assertEquals(NimTokenTypes.CHAR, only("'a'"))
    @Test fun `char escape newline`() = assertEquals(NimTokenTypes.CHAR, only("'\\n'"))
    @Test fun `char escape backslash`() = assertEquals(NimTokenTypes.CHAR, only("'\\\\'"))

    // --- Numbers ---

    @Test fun `decimal integer`() = assertEquals(NimTokenTypes.NUMBER, only("42"))
    @Test fun zero() = assertEquals(NimTokenTypes.NUMBER, only("0"))
    @Test fun float() = assertEquals(NimTokenTypes.NUMBER, only("3.14"))
    @Test fun `float exponent`() = assertEquals(NimTokenTypes.NUMBER, only("1e10"))
    @Test fun `float negative exponent`() = assertEquals(NimTokenTypes.NUMBER, only("1e-10"))
    @Test fun `float positive exponent`() = assertEquals(NimTokenTypes.NUMBER, only("1e+10"))
    @Test fun `hex lowercase x`() = assertEquals(NimTokenTypes.NUMBER, only("0xff"))
    @Test fun `hex uppercase X`() = assertEquals(NimTokenTypes.NUMBER, only("0XFF"))
    @Test fun `hex mixed case digits`() = assertEquals(NimTokenTypes.NUMBER, only("0xDeAdBeEf"))
    @Test fun octal() = assertEquals(NimTokenTypes.NUMBER, only("0o77"))
    @Test fun binary() = assertEquals(NimTokenTypes.NUMBER, only("0b1010"))
    @Test fun `underscore separator`() = assertEquals(NimTokenTypes.NUMBER, only("1_000_000"))
    @Test fun `int suffix i8`() = assertEquals(NimTokenTypes.NUMBER, only("42'i8"))
    @Test fun `int suffix i16`() = assertEquals(NimTokenTypes.NUMBER, only("42'i16"))
    @Test fun `int suffix i32`() = assertEquals(NimTokenTypes.NUMBER, only("42'i32"))
    @Test fun `int suffix i64`() = assertEquals(NimTokenTypes.NUMBER, only("42'i64"))
    @Test fun `int suffix u`() = assertEquals(NimTokenTypes.NUMBER, only("42'u"))
    @Test fun `int suffix u8`() = assertEquals(NimTokenTypes.NUMBER, only("42'u8"))
    @Test fun `int suffix u32`() = assertEquals(NimTokenTypes.NUMBER, only("42'u32"))
    @Test fun `int suffix u64`() = assertEquals(NimTokenTypes.NUMBER, only("0'u64"))
    @Test fun `float suffix f`() = assertEquals(NimTokenTypes.NUMBER, only("1.0'f"))
    @Test fun `float suffix f32`() = assertEquals(NimTokenTypes.NUMBER, only("1.0'f32"))
    @Test fun `float suffix f64`() = assertEquals(NimTokenTypes.NUMBER, only("1.0'f64"))
    @Test fun `float suffix d`() = assertEquals(NimTokenTypes.NUMBER, only("1.0'd"))

    // --- Identifiers ---

    @Test fun `simple identifier`() = assertEquals(NimTokenTypes.IDENTIFIER, only("myVar"))
    @Test fun `uppercase identifier`() = assertEquals(NimTokenTypes.IDENTIFIER, only("MyType"))
    @Test fun `identifier with digits`() = assertEquals(NimTokenTypes.IDENTIFIER, only("x1"))
    @Test fun `identifier with underscore`() = assertEquals(NimTokenTypes.IDENTIFIER, only("my_var"))
    @Test fun `backtick identifier`() = assertEquals(NimTokenTypes.IDENTIFIER, only("`my ident`"))
    @Test fun `backtick operator identifier`() = assertEquals(NimTokenTypes.IDENTIFIER, only("`+`"))
    @Test fun `backtick keyword as identifier`() = assertEquals(NimTokenTypes.IDENTIFIER, only("`proc`"))

    // --- Brackets ---

    @Test fun lparen() = assertEquals(NimTokenTypes.LPAREN, only("("))
    @Test fun rparen() = assertEquals(NimTokenTypes.RPAREN, only(")"))
    @Test fun lbracket() = assertEquals(NimTokenTypes.LBRACKET, only("["))
    @Test fun rbracket() = assertEquals(NimTokenTypes.RBRACKET, only("]"))
    @Test fun lbrace() = assertEquals(NimTokenTypes.LBRACE, only("{"))
    @Test fun rbrace() = assertEquals(NimTokenTypes.RBRACE, only("}"))

    // --- Operators (catch-all) ---

    @Test fun plus() = assertEquals(NimTokenTypes.OPERATOR, only("+"))
    @Test fun colon() = assertEquals(NimTokenTypes.OPERATOR, only(":"))
    @Test fun dot() = assertEquals(NimTokenTypes.OPERATOR, only("."))
    @Test fun equals() = assertEquals(NimTokenTypes.OPERATOR, only("="))
    @Test fun comma() = assertEquals(NimTokenTypes.OPERATOR, only(","))
    @Test fun semicolon() = assertEquals(NimTokenTypes.OPERATOR, only(";"))

    // --- Whitespace ---

    @Test fun spaces() = assertEquals(TokenType.WHITE_SPACE, only("   "))
    @Test fun newline() = assertEquals(TokenType.WHITE_SPACE, only("\n"))
    @Test fun tab() = assertEquals(TokenType.WHITE_SPACE, only("\t"))
    @Test fun `mixed whitespace`() = assertEquals(TokenType.WHITE_SPACE, only("  \t\n  "))

    // --- Integration ---

    @Test fun `proc declaration`() {
        assertEquals(
            listOf(
                NimTokenTypes.KEYWORD,    // proc
                TokenType.WHITE_SPACE,
                NimTokenTypes.IDENTIFIER, // foo
                NimTokenTypes.LPAREN,
                NimTokenTypes.IDENTIFIER, // x
                NimTokenTypes.OPERATOR,   // :
                TokenType.WHITE_SPACE,
                NimTokenTypes.IDENTIFIER, // int
                NimTokenTypes.RPAREN,
                NimTokenTypes.OPERATOR,   // :
                TokenType.WHITE_SPACE,
                NimTokenTypes.IDENTIFIER, // string
                TokenType.WHITE_SPACE,
                NimTokenTypes.OPERATOR,   // =
            ),
            types("proc foo(x: int): string =")
        )
    }

    @Test fun `comment after code`() {
        assertEquals(
            listOf(NimTokenTypes.IDENTIFIER, TokenType.WHITE_SPACE, NimTokenTypes.LINE_COMMENT),
            types("x # comment")
        )
    }

    @Test fun `block comment between tokens`() {
        assertEquals(
            listOf(NimTokenTypes.IDENTIFIER, NimTokenTypes.BLOCK_COMMENT, NimTokenTypes.IDENTIFIER),
            types("a#[]#b")
        )
    }
}
