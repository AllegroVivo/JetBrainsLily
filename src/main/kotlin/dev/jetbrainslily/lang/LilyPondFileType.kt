package dev.jetbrainslily.lang

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

/**
 * Tells the IDE "here's what a LilyPond file is": which Language it maps to,
 * its default extension, and what to show in the Project view. plugin.xml's
 * <fileType> extension point wires this singleton in — see fieldName="INSTANCE".
 */
object LilyPondFileType : LanguageFileType(LilyPondLanguage) {

    override fun getName(): String = "LilyPond"

    override fun getDescription(): String = "LilyPond music engraving source file"

    override fun getDefaultExtension(): String = "ly"

    override fun getIcon(): Icon = LilyPondIcons.FILE
}
