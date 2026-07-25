package dev.jetbrainslily.lang

import com.intellij.openapi.util.IconLoader

/**
 * IconLoader.getIcon caches by (path, classloader) and picks up dark/light
 * theme variants automatically if a "_dark" suffixed file exists next to
 * the base one — we only have one variant for now, so it's used as-is in
 * both themes.
 */
object LilyPondIcons {
    @JvmField
    val FILE = IconLoader.getIcon("/icons/lilypond.svg", LilyPondIcons::class.java)
}
