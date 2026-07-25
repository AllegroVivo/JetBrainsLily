package dev.jetbrainslily.lang.psi

import com.intellij.psi.tree.IElementType
import dev.jetbrainslily.lang.LilyPondLanguage

/**
 * IElementType is the platform's currency for "what kind of token/node is
 * this" — both the lexer (tokens) and, later, the parser (PSI nodes) speak
 * in these. Every distinct kind gets its own singleton instance; identity
 * comparison (===), not string comparison, is how the platform tells them
 * apart everywhere else (highlighting, brace matching, folding...).
 */
class LilyPondTokenType(debugName: String) : IElementType(debugName, LilyPondLanguage) {
    override fun toString(): String = "LilyPondTokenType.${super.toString()}"
}

object LilyPondTokenTypes {
    val COMMENT = LilyPondTokenType("COMMENT")
    val STRING = LilyPondTokenType("STRING")
    val NUMBER = LilyPondTokenType("NUMBER")
    val KEYWORD = LilyPondTokenType("KEYWORD")
    // Each bracket shape gets its own type rather than one shared BRACE.
    // A shared type was fine for coloring (they're all colored alike) but
    // brace matching pairs by token type identity, so '{' and '<' must be
    // distinguishable or the matcher can't tell which closer goes with
    // which opener.
    val LBRACE = LilyPondTokenType("LBRACE") // {
    val RBRACE = LilyPondTokenType("RBRACE") // }
    val LANGLE = LilyPondTokenType("LANGLE") // < (chord open, e.g. <c e g>4)
    val RANGLE = LilyPondTokenType("RANGLE") // > (chord close)
    val BARLINE = LilyPondTokenType("BARLINE")

    // "<<" / ">>" (simultaneous music) are their own two-character tokens,
    // distinct from single '<'/'>' above.
    val DOUBLE_ANGLE_OPEN = LilyPondTokenType("DOUBLE_ANGLE_OPEN")
    val DOUBLE_ANGLE_CLOSE = LilyPondTokenType("DOUBLE_ANGLE_CLOSE")

    // A lexer only sees shape, not meaning: "c" (a pitch) and "treble" (a
    // markup argument) are both just letters, indistinguishable without
    // parser context. WORD covers both until the parser can tell them apart.
    val WORD = LilyPondTokenType("WORD")

    // Everything else, one character at a time: ~ ( ) - etc. Each mark is
    // individually meaningful in LilyPond, so we don't group them into runs.
    val PUNCTUATION = LilyPondTokenType("PUNCTUATION")
}
