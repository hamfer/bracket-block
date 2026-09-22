package com.github.hamfer.bracketblock.highlighter

/**
 * Represents a range to be highlighted in the editor.
 *
 * @param startOffset Start offset of the scope (inclusive)
 * @param endOffset End offset of the scope (exclusive)
 * @param bracketStartOffset Optional: offset of the opening bracket
 * @param bracketEndOffset Optional: offset after the closing bracket
 */
data class HighlightRange(
    val startOffset: Int,
    val endOffset: Int,
    val bracketStartOffset: Int? = null,
    val bracketEndOffset: Int? = null
)
