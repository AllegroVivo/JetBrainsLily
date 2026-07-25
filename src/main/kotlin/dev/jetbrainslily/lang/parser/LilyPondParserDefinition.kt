package dev.jetbrainslily.lang.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import dev.jetbrainslily.lang.LilyPondLanguage
import dev.jetbrainslily.lang.lexer.LilyPondLexer
import dev.jetbrainslily.lang.psi.LilyPondFile
import dev.jetbrainslily.lang.psi.LilyPondTokenTypes

/**
 * The extension point (<lang.parserDefinition> in plugin.xml) that tells
 * the platform how to build a real PSI tree for LilyPond files: which
 * lexer feeds the parser, which token types are whitespace/comments/
 * strings (so generic platform features — e.g. "select word", string
 * manipulation intentions — work without LilyPond-specific code), and
 * which PsiFile subclass represents a parsed file.
 */
class LilyPondParserDefinition : ParserDefinition {

    override fun createLexer(project: Project): Lexer = LilyPondLexer()

    override fun createParser(project: Project): PsiParser = LilyPondParser()

    override fun getFileNodeType(): IFileElementType = FILE

    // PsiBuilder consults these to auto-skip whitespace/comments while the
    // parser runs — that's why LilyPondParser never has to handle them
    // explicitly. They still end up attached to the tree as trivia.
    override fun getWhitespaceTokens(): TokenSet = TokenSet.create(TokenType.WHITE_SPACE)

    override fun getCommentTokens(): TokenSet = TokenSet.create(LilyPondTokenTypes.COMMENT)

    override fun getStringLiteralElements(): TokenSet = TokenSet.create(LilyPondTokenTypes.STRING)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = LilyPondFile(viewProvider)

    // ASTWrapperPsiElement is the platform's generic default PSI class for
    // a composite node — fine until a node type needs real accessor
    // methods (e.g. "give me this block's child music elements"), at
    // which point we'd write a dedicated PSI class for it instead.
    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    companion object {
        val FILE = IFileElementType(LilyPondLanguage)
    }
}
