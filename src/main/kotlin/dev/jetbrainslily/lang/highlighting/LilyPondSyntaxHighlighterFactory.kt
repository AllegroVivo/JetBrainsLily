package dev.jetbrainslily.lang.highlighting

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * The platform asks a factory (not the highlighter directly) so it can
 * hand back a different highlighter per file/project if a language ever
 * needs that (e.g. version-dependent syntax). We don't need that yet, so
 * this always returns a fresh instance.
 */
class LilyPondSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        LilyPondSyntaxHighlighter()
}
