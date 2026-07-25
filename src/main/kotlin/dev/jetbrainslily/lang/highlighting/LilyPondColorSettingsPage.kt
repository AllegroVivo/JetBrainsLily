package dev.jetbrainslily.lang.highlighting

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.jetbrainslily.lang.LilyPondIcons
import javax.swing.Icon

/**
 * Settings > Editor > Color Scheme > LilyPond. getHighlighter() returns
 * the real LilyPondSyntaxHighlighter, so the preview pane colors the demo
 * text below using the exact same lexer-driven logic a real .ly file gets
 * — no separate "preview-only" highlighting rules to keep in sync, and no
 * <tag> markup needed in the demo text (that mechanism is only for
 * highlighting a lexer can't derive on its own, e.g. semantic distinctions
 * from an Annotator — we don't have any of those yet).
 */
class LilyPondColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = LilyPondIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = LilyPondSyntaxHighlighter()

    override fun getDemoText(): String = DEMO_TEXT

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "LilyPond"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Comment", LilyPondSyntaxHighlighter.COMMENT),
            AttributesDescriptor("String", LilyPondSyntaxHighlighter.STRING),
            AttributesDescriptor("Number/duration", LilyPondSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Keyword", LilyPondSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Braces", LilyPondSyntaxHighlighter.BRACE),
            AttributesDescriptor("Word (note or markup)", LilyPondSyntaxHighlighter.WORD),
            AttributesDescriptor("Barline", LilyPondSyntaxHighlighter.BARLINE),
            AttributesDescriptor("Punctuation", LilyPondSyntaxHighlighter.PUNCTUATION),
        )

        // Exercises every configurable key at least once: comment (line +
        // block), string, keyword, braces + double-angle + chord angles,
        // word, number, barline, and punctuation (slur parens, tie).
        private const val DEMO_TEXT = """% A simple LilyPond example
\version "2.24.0"

\score {
  \new Staff {
    \relative c' {
      c4( d e) f | g2 a4 b~ |
      b1 <c e g>4 r2.
      %{ a block comment %}
    }
  }
}
"""
    }
}
