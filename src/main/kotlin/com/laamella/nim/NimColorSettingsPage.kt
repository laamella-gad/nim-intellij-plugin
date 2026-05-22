package com.laamella.nim

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import javax.swing.Icon

class NimColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Comment//Line comment", NimHighlightingColors.LINE_COMMENT),
        AttributesDescriptor("Comment//Doc comment", NimHighlightingColors.DOC_COMMENT),
        AttributesDescriptor("Comment//Block comment", NimHighlightingColors.BLOCK_COMMENT),
        AttributesDescriptor("Keyword", NimHighlightingColors.KEYWORD),
        AttributesDescriptor("String", NimHighlightingColors.STRING),
        AttributesDescriptor("Number", NimHighlightingColors.NUMBER),
        AttributesDescriptor("Identifier", NimHighlightingColors.IDENTIFIER),
        AttributesDescriptor("Operator", NimHighlightingColors.OPERATOR),
        AttributesDescriptor("Braces//Parenthesis", NimHighlightingColors.PAREN),
        AttributesDescriptor("Braces//Bracket", NimHighlightingColors.BRACKET),
        AttributesDescriptor("Braces//Brace", NimHighlightingColors.BRACE),
    )

    override fun getIcon(): Icon = NimIcons.FILE
    override fun getHighlighter(): SyntaxHighlighter = NimSyntaxHighlighter()
    override fun getDisplayName(): String = "Nim"
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = descriptors
    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getDemoText(): String = """
        # Line comment
        ## Doc comment
        #[ Block comment ]#

        const MAX = 100
        var x: int = 42
        let s = "hello, world"
        let c = 'a'

        proc add(a, b: int): int =
          a + b

        let arr = [1, 2, 3]
        let tup = (x: 1, y: 2)
        let t = {red, green}

        type Color = enum
          red, green, blue
    """.trimIndent()
}
