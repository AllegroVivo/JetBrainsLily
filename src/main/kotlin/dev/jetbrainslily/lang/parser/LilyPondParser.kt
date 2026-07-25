package dev.jetbrainslily.lang.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import dev.jetbrainslily.lang.psi.LilyPondElementTypes
import dev.jetbrainslily.lang.psi.LilyPondTokenTypes

/**
 * Turns LilyPondLexer's flat token stream into a tree. PsiBuilder is the
 * platform's tool for this: mark() remembers "a node starts here" without
 * committing to what kind yet, and later done(type)/error(message)
 * retroactively wraps everything produced since that mark into one node
 * of that type. That's what makes recursion natural here — parseBlock
 * calls parseItem, which can itself call parseBlock again for a nested
 * `{ }`, and each level's mark only closes over its own contents.
 *
 * Scope, deliberately: block nesting (`{ }` / `<< >>`), notes (a pitch
 * WORD + its duration NUMBER), and commands (`\keyword` + an optional
 * single WORD/STRING argument + an optional following block, all as one
 * node — so the block a command introduces actually belongs to it in the
 * tree, e.g. `\new Staff { ... }` is ONE node, not three unrelated
 * siblings). What's still NOT modeled: multi-argument commands
 * (`\context Voice = "one" { ... }` — the '=' breaks the pattern, so the
 * block stays unattached), non-word/string arguments (`\times 2/3 { ... }`
 * — same problem), and note+duration grouping doesn't check for actual
 * character adjacency (it'll still pair "c" with a NUMBER that follows it
 * even across whitespace — a PsiBuilder-level simplification, not a
 * deliberate design choice).
 */
class LilyPondParser : PsiParser {

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val rootMarker = builder.mark()
        while (!builder.eof()) {
            parseItem(builder)
        }
        rootMarker.done(root)
        return builder.treeBuilt
    }

    private fun parseItem(builder: PsiBuilder) {
        // Now that each bracket shape has its own token type (see
        // LilyPondTokenTypes — split from a shared BRACE type specifically
        // to support this), matching is plain type identity — no more
        // comparing token text to tell "{" apart from "<".
        when (builder.tokenType) {
            LilyPondTokenTypes.LBRACE ->
                parseBlock(builder, LilyPondElementTypes.SEQUENTIAL_MUSIC, LilyPondTokenTypes.RBRACE)

            LilyPondTokenTypes.DOUBLE_ANGLE_OPEN ->
                parseBlock(builder, LilyPondElementTypes.SIMULTANEOUS_MUSIC, LilyPondTokenTypes.DOUBLE_ANGLE_CLOSE)

            LilyPondTokenTypes.KEYWORD -> parseCommand(builder)

            LilyPondTokenTypes.WORD -> parseWord(builder)

            // Anything else (number, string, barline, punctuation, a stray
            // closing delimiter with no opener) is just a leaf — consume
            // one token and move on. No wrapping needed for tokens that
            // don't introduce nesting.
            else -> builder.advanceLexer()
        }
    }

    // "c4" -> one NOTE node wrapping WORD("c") + NUMBER("4"), instead of
    // two unrelated sibling leaves. lookAhead peeks past the current token
    // without consuming it (and, like tokenType/advanceLexer, skips
    // whitespace/comments automatically) — that's the "doesn't check real
    // adjacency" simplification noted above: it can't tell "c4" from "c 4".
    private fun parseWord(builder: PsiBuilder) {
        if (builder.lookAhead(1) == LilyPondTokenTypes.NUMBER) {
            val marker = builder.mark()
            builder.advanceLexer() // pitch
            builder.advanceLexer() // duration
            marker.done(LilyPondElementTypes.NOTE)
        } else {
            builder.advanceLexer() // bare word, no duration attached
        }
    }

    // "\new Staff { ... }" -> one COMMAND node wrapping the keyword, its
    // argument, and its block — instead of three unrelated siblings. The
    // argument check is a plain advanceLexer(), not parseItem(), so it
    // never accidentally pairs the argument word with a following NUMBER
    // as if it were a NOTE.
    private fun parseCommand(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume \keyword

        if (builder.tokenType == LilyPondTokenTypes.WORD || builder.tokenType == LilyPondTokenTypes.STRING) {
            builder.advanceLexer() // consume the single argument, if present
        }

        when (builder.tokenType) {
            LilyPondTokenTypes.LBRACE ->
                parseBlock(builder, LilyPondElementTypes.SEQUENTIAL_MUSIC, LilyPondTokenTypes.RBRACE)
            LilyPondTokenTypes.DOUBLE_ANGLE_OPEN ->
                parseBlock(builder, LilyPondElementTypes.SIMULTANEOUS_MUSIC, LilyPondTokenTypes.DOUBLE_ANGLE_CLOSE)
            // No block follows — a bare command citation (e.g. \break),
            // nothing more to attach.
        }

        marker.done(LilyPondElementTypes.COMMAND)
    }

    private fun parseBlock(builder: PsiBuilder, elementType: IElementType, closingTokenType: IElementType) {
        val marker = builder.mark()
        builder.advanceLexer() // consume the opening delimiter

        while (!builder.eof() && builder.tokenType != closingTokenType) {
            parseItem(builder)
        }

        if (builder.eof()) {
            // Ran out of file before finding the matching closer. Still
            // close the marker (as an error node) rather than leaving it
            // dangling — PsiBuilder requires every mark() to be resolved.
            marker.error("Unclosed block")
        } else {
            builder.advanceLexer() // consume the closing delimiter
            marker.done(elementType)
        }
    }
}
