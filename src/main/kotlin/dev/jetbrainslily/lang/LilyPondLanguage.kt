package dev.jetbrainslily.lang

import com.intellij.lang.Language

/**
 * Every custom language plugin starts with exactly one of these: a singleton
 * that identifies "LilyPond" as a distinct language to the platform. Nothing
 * about parsing lives here yet — this object is just an ID that other
 * extensions (file type, lexer, parser, highlighter...) will point back to.
 */
object LilyPondLanguage : Language("LilyPond") {

    private fun readResolve(): Any = LilyPondLanguage
}
