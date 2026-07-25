package dev.jetbrainslily.lang.structure

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * The extension point (<lang.psiStructureViewFactory>) the platform calls
 * to get a builder whenever the Structure tool window opens for a .ly
 * file. TreeBasedStructureViewBuilder is the standard builder for a plain
 * tree (as opposed to e.g. a flat list) — we only need to say how to
 * construct the model.
 */
class LilyPondStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel =
                LilyPondStructureViewModel(psiFile, editor)
        }
}
