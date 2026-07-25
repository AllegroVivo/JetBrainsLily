package dev.jetbrainslily.lang.psi

import com.intellij.psi.tree.IElementType
import dev.jetbrainslily.lang.LilyPondLanguage

/**
 * Composite (non-leaf) node types — what the parser produces, as opposed
 * to LilyPondTokenTypes, which are the leaves it consumes. A parser's job
 * is turning a flat token stream into a tree of these: e.g. an inner
 * `{ }` becomes its own SEQUENTIAL_MUSIC node nested inside whatever
 * contains it, so the platform finally knows which brace pairs with which
 * — something a lexer, which only sees one token at a time, structurally
 * cannot know.
 */
class LilyPondElementType(debugName: String) : IElementType(debugName, LilyPondLanguage)

object LilyPondElementTypes {
    val SEQUENTIAL_MUSIC = LilyPondElementType("SEQUENTIAL_MUSIC")   // { ... }
    val SIMULTANEOUS_MUSIC = LilyPondElementType("SIMULTANEOUS_MUSIC") // << ... >>

    // A pitch WORD paired with its duration NUMBER, e.g. "c4" -> one NOTE
    // node instead of two unrelated sibling leaves.
    val NOTE = LilyPondElementType("NOTE")

    // \keyword + an optional single WORD/STRING argument + an optional
    // following block, all as one node — e.g. "\new Staff { ... }" instead
    // of three unrelated siblings. This is what lets the block actually
    // belong to the command that introduced it.
    val COMMAND = LilyPondElementType("COMMAND")
}
