package dev.jetbrainslily.lang.structure

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * StructureViewModelBase supplies the generic wiring (root element,
 * current-editor-position tracking); we only need to say what the root is
 * and how the tree should behave — never force an expand arrow on empty
 * nodes, and nothing is ever a fixed leaf (a block found empty today could
 * still gain children later as the file is edited).
 */
class LilyPondStructureViewModel(psiFile: PsiFile, editor: Editor?) :
    StructureViewModelBase(psiFile, editor, LilyPondStructureViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {

    init {
        withSuitableClasses(PsiFile::class.java)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean = false
}
