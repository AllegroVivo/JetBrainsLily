package dev.jetbrainslily.lang.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import dev.jetbrainslily.lang.psi.LilyPondTokenTypes

/**
 * Hand-written lexer: given the whole file as a CharSequence, chop it into
 * tokens one at a time. Real-world plugins usually generate this from a
 * JFlex grammar instead of writing it by hand, but LilyPond's tokens are
 * simple enough that hand-writing one first is a better way to learn what
 * a Lexer actually has to do before reaching for a generator.
 *
 * The platform drives this via start()/advance() one token at a time (pull
 * model, not "tokenize the whole file up front") — editors call advance()
 * lazily as they need more tokens, e.g. while scrolling.
 */
class LilyPondLexer : LexerBase() {
    private lateinit var buffer: CharSequence
    private var endOffset = 0
    private var tokenStart = 0
    private var tokenEnd = 0
    private var currentToken: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.endOffset = endOffset
        tokenStart = startOffset
        tokenEnd = startOffset
        advance()
    }

    // We have no lexer states (string interpolation, nested comments, etc.
    // would need one) — always 0. Returning a real value only matters if a
    // parser needs to resume mid-file (e.g. after an incremental edit).
    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = currentToken
    override fun getTokenStart(): Int = tokenStart
    override fun getTokenEnd(): Int = tokenEnd
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = endOffset

    override fun advance() {
        tokenStart = tokenEnd
        if (tokenStart >= endOffset) {
            currentToken = null
            return
        }
        currentToken = when (val c = buffer[tokenStart]) {
            in WHITESPACE -> lexWhitespace()
            '%' -> lexComment()
            '"' -> lexString()
            '\\' -> lexKeyword()
            in '0'..'9' -> lexNumber()
            '{' -> lexSingleChar(LilyPondTokenTypes.LBRACE)
            '}' -> lexSingleChar(LilyPondTokenTypes.RBRACE)
            '<' -> if (peek(1) == '<') lexTwoChars(LilyPondTokenTypes.DOUBLE_ANGLE_OPEN) else lexSingleChar(LilyPondTokenTypes.LANGLE)
            '>' -> if (peek(1) == '>') lexTwoChars(LilyPondTokenTypes.DOUBLE_ANGLE_CLOSE) else lexSingleChar(LilyPondTokenTypes.RANGLE)
            '|' -> lexSingleChar(LilyPondTokenTypes.BARLINE)
            else -> if (c.isLetter()) lexWord() else lexPunctuation()
        }
    }

    private fun lexWhitespace(): IElementType {
        var i = tokenStart
        while (i < endOffset && buffer[i] in WHITESPACE) i++
        tokenEnd = i
        return TokenType.WHITE_SPACE
    }

    private fun lexComment(): IElementType {
        // Block comment: %{ ... %}
        if (tokenStart + 1 < endOffset && buffer[tokenStart + 1] == '{') {
            var i = tokenStart + 2
            while (i + 1 < endOffset && !(buffer[i] == '%' && buffer[i + 1] == '}')) i++
            tokenEnd = if (i + 1 < endOffset) i + 2 else endOffset
            return LilyPondTokenTypes.COMMENT
        }
        // Line comment: % to end of line
        var i = tokenStart
        while (i < endOffset && buffer[i] != '\n') i++
        tokenEnd = i
        return LilyPondTokenTypes.COMMENT
    }

    private fun lexString(): IElementType {
        var i = tokenStart + 1
        while (i < endOffset && buffer[i] != '"') {
            if (buffer[i] == '\\' && i + 1 < endOffset) i++ // skip escaped char
            i++
        }
        if (i < endOffset) i++ // consume closing quote
        tokenEnd = i
        return LilyPondTokenTypes.STRING
    }

    private fun lexKeyword(): IElementType {
        var i = tokenStart + 1
        while (i < endOffset && buffer[i].isLetter()) i++
        tokenEnd = if (i == tokenStart + 1) minOf(tokenStart + 2, endOffset) else i
        return LilyPondTokenTypes.KEYWORD
    }

    private fun lexNumber(): IElementType {
        var i = tokenStart
        // Duration numbers can carry trailing dots (8. = dotted eighth),
        // so a dot immediately after digits stays part of the same token.
        while (i < endOffset && (buffer[i] in '0'..'9' || buffer[i] == '.')) i++
        tokenEnd = i
        return LilyPondTokenTypes.NUMBER
    }

    private fun lexSingleChar(type: IElementType): IElementType {
        tokenEnd = tokenStart + 1
        return type
    }

    private fun lexTwoChars(type: IElementType): IElementType {
        tokenEnd = tokenStart + 2
        return type
    }

    private fun peek(offset: Int): Char? {
        val i = tokenStart + offset
        return if (i < endOffset) buffer[i] else null
    }

    // Letters only — stops before the first digit, so "c4" lexes as
    // WORD("c") followed by a separate NUMBER("4") token, not one blob.
    // Trailing octave marks (' or ,) are swept in since they always attach
    // directly to a preceding pitch name (cis''', d,,).
    private fun lexWord(): IElementType {
        var i = tokenStart
        while (i < endOffset && buffer[i].isLetter()) i++
        while (i < endOffset && (buffer[i] == '\'' || buffer[i] == ',')) i++
        tokenEnd = i
        return LilyPondTokenTypes.WORD
    }

    // One character at a time — ~ ( ) - etc. are each individually
    // meaningful (ties, slurs, articulation shorthand), so we don't lump
    // them into runs the way lexWord groups letters.
    private fun lexPunctuation(): IElementType {
        tokenEnd = tokenStart + 1
        return LilyPondTokenTypes.PUNCTUATION
    }

    companion object {
        private val WHITESPACE = charArrayOf(' ', '\t', '\r', '\n')
    }
}
