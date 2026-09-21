package com.github.hamfer.bracketblock.listeners

import com.github.hamfer.bracketblock.highlighter.BracketBlockHighlighter
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.RangeHighlighter

class BracketBlockCaretListener(private val editor: Editor) : CaretListener, Disposable {
    private val highlighters = ArrayList<RangeHighlighter>()
    init { editor.caretModel.addCaretListener(this) }
    override fun caretPositionChanged(event: CaretEvent) {
        val highlighter = BracketBlockHighlighter(event.editor)
        highlighter.clearHighlight(highlighters)
        highlighters.clear()
        highlighters += highlighter.highlightBracketBlock(highlighter.findClosetBracePair(event.editor.caretModel.offset))
    }
    override fun dispose() { editor.caretModel.removeCaretListener(this); highlighters.clear() }
}
