package dev.jetbrainslily.lang.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.jetbrainslily.lang.psi.LilyPondElementTypes
import dev.jetbrainslily.lang.psi.LilyPondTokenTypes

/**
 * One row in the Structure View tree. Wraps a PSI element — the file
 * itself at the root, or one of our COMMAND/SEQUENTIAL_MUSIC/
 * SIMULTANEOUS_MUSIC composite nodes — and tells the tree UI three things:
 * what label to show (getPresentation), what's nested under it
 * (getChildren), and what to jump to on double-click (navigate).
 */
class LilyPondStructureViewElement(private val element: PsiElement) : StructureViewTreeElement {

    override fun getValue(): Any = element

    override fun navigate(requestFocus: Boolean) {
        (element as? Navigatable)?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (element as? Navigatable)?.canNavigate() ?: false

    override fun canNavigateToSource(): Boolean = canNavigate()

    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String = describe(element)
        override fun getIcon(unused: Boolean) = null
    }

    override fun getChildren(): Array<TreeElement> =
        structuralChildren(element)
            .map { LilyPondStructureViewElement(it) }
            .toTypedArray()

    // A COMMAND's own block body is transparent for display purposes —
    // its content becomes this row's children directly, without an
    // intermediate "{ }" row for the block itself (the command row
    // already implies "here's its body").
    private fun structuralChildren(e: PsiElement): List<PsiElement> {
        val searchRoot = if (isCommand(e)) {
            e.children.firstOrNull(::isBlock) ?: return emptyList()
        } else {
            e
        }
        return searchRoot.children.filter { isCommand(it) || isBlock(it) }
    }

    private fun isCommand(e: PsiElement): Boolean = e.node?.elementType == LilyPondElementTypes.COMMAND

    private fun isBlock(e: PsiElement): Boolean {
        val type = e.node?.elementType
        return type == LilyPondElementTypes.SEQUENTIAL_MUSIC || type == LilyPondElementTypes.SIMULTANEOUS_MUSIC
    }

    private fun describe(e: PsiElement): String = when {
        e is PsiFile -> e.name
        isCommand(e) -> commandLabel(e)
        e.node?.elementType == LilyPondElementTypes.SIMULTANEOUS_MUSIC -> "<< >>"
        else -> "{ }"
    }

    // Reads the command's own keyword + optional argument straight off
    // the tree — no more guessing from neighboring tokens. Doesn't handle
    // multi-argument commands (\context Voice = "one" { ... — the parser
    // itself doesn't attach the block in that case either, see
    // LilyPondParser's parseCommand).
    //
    // Deliberately uses ASTNode children here, not PsiElement.children:
    // PsiElement.getChildren() only returns composite (non-leaf) children
    // — KEYWORD/WORD are leaf tokens, so that filtered view always came up
    // empty. ASTNode.getChildren(null) returns every direct child,
    // leaves included.
    private fun commandLabel(command: PsiElement): String {
        val children = command.node?.getChildren(null) ?: return ""
        val keyword = children.firstOrNull { it.elementType == LilyPondTokenTypes.KEYWORD }
        val arg = children.firstOrNull {
            it.elementType == LilyPondTokenTypes.WORD || it.elementType == LilyPondTokenTypes.STRING
        }
        return listOfNotNull(keyword?.text, arg?.text).joinToString(" ")
    }
}
