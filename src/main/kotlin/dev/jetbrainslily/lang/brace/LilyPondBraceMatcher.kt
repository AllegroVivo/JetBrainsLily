package dev.jetbrainslily.lang.brace

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import dev.jetbrainslily.lang.psi.LilyPondTokenTypes

/**
 * Tells the editor which token types pair up, so it can highlight the
 * matching brace as the caret sits next to one and support "jump to
 * matching brace" / "select to matching brace". This is a different
 * mechanism from the parser's own block matching: the parser only builds
 * real tree nodes for `{ }` and `<< >>` (the pairs that create actual
 * nesting structure). Chord brackets `< >` are included here too, purely
 * for visual/navigation matching, even though the parser leaves them as
 * flat, unstructured tokens — see the `structural` flag below.
 */
class LilyPondBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = PAIRS

    // Whether it's OK for the editor to auto-insert a closer right before
    // this token type without getting confused. No LilyPond-specific
    // restriction needed yet.
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        private val PAIRS = arrayOf(
            // structural=true: the parser actually builds a tree node for
            // this pair (SEQUENTIAL_MUSIC / SIMULTANEOUS_MUSIC).
            BracePair(LilyPondTokenTypes.LBRACE, LilyPondTokenTypes.RBRACE, true),
            BracePair(LilyPondTokenTypes.DOUBLE_ANGLE_OPEN, LilyPondTokenTypes.DOUBLE_ANGLE_CLOSE, true),
            // structural=false: chord brackets — visually matched, but not
            // (yet) represented as their own composite node in the tree.
            BracePair(LilyPondTokenTypes.LANGLE, LilyPondTokenTypes.RANGLE, false),
        )
    }
}
