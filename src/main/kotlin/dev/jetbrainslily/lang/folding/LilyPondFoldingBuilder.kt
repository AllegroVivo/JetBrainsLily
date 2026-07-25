package dev.jetbrainslily.lang.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiElement
import dev.jetbrainslily.lang.psi.LilyPondElementTypes

/**
 * Folding is a good first proof that the parser's nesting is actually
 * correct — it walks the composite nodes LilyPondParser built and offers
 * one fold region per block. If block matching were wrong (e.g. an inner
 * `{` accidentally closed by an outer file's `}`), folding would visibly
 * collapse the wrong range instead of just silently being incorrect.
 */
class LilyPondFoldingBuilder : FoldingBuilderEx() {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        collectBlocks(root.node, descriptors)
        return descriptors.toTypedArray()
    }

    private fun collectBlocks(node: ASTNode, descriptors: MutableList<FoldingDescriptor>) {
        if (node.elementType == LilyPondElementTypes.SEQUENTIAL_MUSIC ||
            node.elementType == LilyPondElementTypes.SIMULTANEOUS_MUSIC
        ) {
            descriptors += FoldingDescriptor(node, node.textRange)
        }
        for (child in node.getChildren(null)) {
            collectBlocks(child, descriptors)
        }
    }

    override fun getPlaceholderText(node: ASTNode): String =
        if (node.elementType == LilyPondElementTypes.SIMULTANEOUS_MUSIC) "<< ... >>" else "{ ... }"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
