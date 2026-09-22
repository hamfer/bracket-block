package com.github.hamfer.bracketblock.listeners

import com.github.hamfer.bracketblock.highlighter.BracketBlockHighlighter
import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.markup.RangeHighlighter

class BracketBlockCaretListener(private val editor: Editor) : CaretListener, Disposable {
    private val highlighterList: ArrayList<RangeHighlighter> = ArrayList()

    init {
        editor.caretModel.addCaretListener(this)
    }

    override fun caretPositionChanged(event: CaretEvent) {
        highlightBracketBlock(event.editor)
    }

    private fun highlightBracketBlock(editor: Editor) {
        val offset = editor.caretModel.offset
        val highlighter = BracketBlockHighlighter(editor)
        val range = highlighter.findClosetBracePairOrScope(offset)
        highlighter.clearHighlight(highlighterList)
        highlighterList.clear()
        highlighter.highlightScope(range)?.let { highlighterList.add(it) }
    }

    override fun dispose() {
        editor.caretModel.removeCaretListener(this)
    }
}
