package dev.jetbrainslily.lang.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider
import dev.jetbrainslily.lang.LilyPondFileType
import dev.jetbrainslily.lang.LilyPondLanguage

/**
 * Every open .ly file gets exactly one of these — the root PSI element
 * that everything else (blocks, tokens) hangs off of. PsiFileBase handles
 * the generic bookkeeping; we just say which Language and FileType it is.
 */
class LilyPondFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, LilyPondLanguage) {
    override fun getFileType() = LilyPondFileType

    override fun toString(): String = "LilyPond File"
}
