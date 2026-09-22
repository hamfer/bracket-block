package com.github.hamfer.bracketblock.highlighter.lang

import com.github.hamfer.bracketblock.highlighter.HighlightRange
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile

/**
 * Per-language highlighter strategy.
 * Implementations encapsulate PSI logic to compute the scope/bracket range for a given caret offset.
 */
interface LanguageHighlighter {
    /** Stable IntelliJ language id this highlighter targets (e.g., JAVA, XML, kotlin, JavaScript). */
    fun id(): String

    /** Human-readable name to be shown in Settings UI. */
    fun displayName(): String = id()

    /** Returns a highlight range for the given offset, or null if none. */
    fun detect(editor: Editor, psiFile: PsiFile, offset: Int): HighlightRange?

    /** Whether this highlighter is available (e.g., required plugin classes are present). */
    fun isAvailable(): Boolean = true

    /**
     * Configuration options exposed by this language highlighter.
     * Each option is identified by a stable key (recommend using FQN of a PSI class) and a label.
     */
    fun options(): List<LanguageOption> = emptyList()
}

data class LanguageOption(val key: String, val label: String, val defaultEnabled: Boolean = true)
