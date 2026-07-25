package dev.jetbrainslily.lang.highlighting

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import dev.jetbrainslily.lang.lexer.LilyPondLexer
import dev.jetbrainslily.lang.psi.LilyPondTokenTypes

/**
 * Maps each token type to a TextAttributesKey — a named color slot, not a
 * literal color. Keys are wired to DefaultLanguageHighlighterColors.* below,
 * so LilyPond automatically inherits whatever colors the user's active
 * theme already assigns to comments/strings/numbers/keywords, with zero
 * extra config. LilyPondColorSettingsPage lists these as configurable so
 * users can override them specifically for LilyPond instead of only ever
 * sharing the generic language defaults.
 */
class LilyPondSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = LilyPondLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        pack(ATTRIBUTES[tokenType])

    companion object {
        val COMMENT: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val STRING: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val KEYWORD: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val BRACE: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_BRACE", DefaultLanguageHighlighterColors.BRACES)

        // These three were left unmapped when the token split happened —
        // no color decision had been made yet. Generic fallback parents:
        // WORD reads as an identifier, BARLINE as a separator/operator,
        // PUNCTUATION as a catch-all "dot"-like mark. None are a perfect
        // semantic fit (LilyPond doesn't have "identifiers" the way a
        // programming language does), but they're reasonable defaults
        // users can now override per-key instead of living with forever.
        val WORD: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_WORD", DefaultLanguageHighlighterColors.IDENTIFIER)
        val BARLINE: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_BARLINE", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PUNCTUATION: TextAttributesKey =
            TextAttributesKey.createTextAttributesKey("LILYPOND_PUNCTUATION", DefaultLanguageHighlighterColors.DOT)

        private val ATTRIBUTES = mapOf(
            LilyPondTokenTypes.COMMENT to COMMENT,
            LilyPondTokenTypes.STRING to STRING,
            LilyPondTokenTypes.NUMBER to NUMBER,
            LilyPondTokenTypes.KEYWORD to KEYWORD,
            LilyPondTokenTypes.LBRACE to BRACE,
            LilyPondTokenTypes.RBRACE to BRACE,
            LilyPondTokenTypes.LANGLE to BRACE,
            LilyPondTokenTypes.RANGLE to BRACE,
            LilyPondTokenTypes.DOUBLE_ANGLE_OPEN to BRACE,
            LilyPondTokenTypes.DOUBLE_ANGLE_CLOSE to BRACE,
            LilyPondTokenTypes.WORD to WORD,
            LilyPondTokenTypes.BARLINE to BARLINE,
            LilyPondTokenTypes.PUNCTUATION to PUNCTUATION,
        )
    }
}
