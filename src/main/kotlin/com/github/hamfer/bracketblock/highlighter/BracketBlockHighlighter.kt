package com.github.hamfer.bracketblock.highlighter

import com.github.hamfer.bracketblock.adapter.BraceMatchingUtilAdapter
import com.github.hamfer.bracketblock.brace.BracePair
import com.github.hamfer.bracketblock.brace.BraceTokenTypes
import com.github.hamfer.bracketblock.settings.LanguageConfiguration
import com.github.hamfer.bracketblock.settings.PluginSettings
import com.github.hamfer.bracketblock.settings.ScopeKind
import com.intellij.lang.Language
import com.intellij.lang.LanguageBraceMatching
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.tree.IElementType
import java.awt.Font

class BracketBlockHighlighter(private val editor: Editor) {
    private val settings = PluginSettings.getInstance()
    private val psiFile: PsiFile? = editor.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(editor.document) }
    private val configuredLanguage: LanguageConfiguration? = psiFile?.language?.let { settings.configurationFor(it.id) }

    fun findClosetBracePair(offset: Int): BracePair? {
        val configuration = configuredLanguage
        // A configured language is deliberately attempted through PSI first. If its
        // language plugin is absent there is no PsiFile/configuration and we use the lexer.
        if (configuration != null && psiFile != null) {
            findPsiPair(offset, configuration)?.let { return it }
        }
        return findManualPair(offset)
    }

    private fun findPsiPair(offset: Int, configuration: LanguageConfiguration): BracePair? {
        if (configuration.kind != ScopeKind.BRACKET) return null
        val at = psiFile?.findElementAt(offset.coerceAtMost(editor.document.textLength)) ?: return null
        val leaves = generateSequence(at) { PsiTreeUtil.prevLeaf(it) }.toList().asReversed() +
            generateSequence(PsiTreeUtil.nextLeaf(at)) { PsiTreeUtil.nextLeaf(it) }.toList()
        val left = leaves.filter { isType(it, configuration.opening) && it.textRange.startOffset <= offset }
            .lastOrNull() ?: return null
        var depth = 0
        for (element in leaves.filter { it.textRange.startOffset >= left.textRange.startOffset }) {
            when {
                isType(element, configuration.opening) -> depth++
                isType(element, configuration.closing) -> { depth--; if (depth == 0) return pair(left, element) }
            }
        }
        return null
    }

    private fun isType(element: PsiElement, symbols: String): Boolean =
        symbols.contains(element.text) && element.firstChild == null && element.textLength == 1

    private fun pair(left: PsiElement, right: PsiElement) = BracePair.BracePairBuilder()
        .leftType(left.node.elementType).rightType(right.node.elementType)
        .leftOffset(left.textRange.startOffset).rightOffset(right.textRange.endOffset).build()

    private fun findManualPair(offset: Int): BracePair? {
        val highlighter = (editor as? EditorEx)?.highlighter ?: return null
        val language = psiFile?.language ?: Language.ANY
        val matcher = LanguageBraceMatching.INSTANCE.forLanguage(language)
        val pairs = matcher?.pairs ?: return null
        val iterator = highlighter.createIterator(offset)
        val text = editor.document.immutableCharSequence
        for (brace in pairs) {
            val left = BraceMatchingUtilAdapter.findLeftLParen(highlighter.createIterator(offset), brace.leftBraceType, text, psiFile?.fileType, editor.settings.isBlockCursor)
            val right = BraceMatchingUtilAdapter.findRightRParen(highlighter.createIterator(offset), brace.rightBraceType, text, psiFile?.fileType, editor.settings.isBlockCursor)
            if (left >= 0 && right >= 0) return BracePair.BracePairBuilder().leftType(brace.leftBraceType).rightType(brace.rightBraceType).leftOffset(left).rightOffset(right).build()
        }
        if (!BraceMatchingUtilAdapter.isStringToken(iterator.tokenType)) return null
        return BracePair.BracePairBuilder().leftType(BraceTokenTypes.DOUBLE_QUOTE).rightType(BraceTokenTypes.DOUBLE_QUOTE).leftOffset(iterator.start).rightOffset(iterator.end).build()
    }

    fun highlightBracketBlock(bracePair: BracePair?): List<RangeHighlighter> {
        if (bracePair == null) return emptyList()
        val result = mutableListOf<RangeHighlighter>()
        val attributes = TextAttributes(null, null, settings.getBorderColor(), EffectType.ROUNDED_BOX, Font.PLAIN)
        if (settings.isHighlightScope()) result += editor.markupModel.addRangeHighlighter(bracePair.leftBrace.offset, bracePair.rightBrace.offset, HighlighterLayer.SELECTION + BracketBlockConstant.HIGHLIGHT_LAYER_WEIGHT, attributes, HighlighterTargetArea.EXACT_RANGE)
        if (settings.isHighlightBrackets()) {
            result += editor.markupModel.addRangeHighlighter(bracePair.leftBrace.offset, bracePair.leftBrace.offset + 1, HighlighterLayer.SELECTION + BracketBlockConstant.HIGHLIGHT_LAYER_WEIGHT + 1, attributes, HighlighterTargetArea.EXACT_RANGE)
            result += editor.markupModel.addRangeHighlighter(bracePair.rightBrace.offset - 1, bracePair.rightBrace.offset, HighlighterLayer.SELECTION + BracketBlockConstant.HIGHLIGHT_LAYER_WEIGHT + 1, attributes, HighlighterTargetArea.EXACT_RANGE)
        }
        return result
    }

    fun clearHighlight(highlighters: List<RangeHighlighter>) = highlighters.forEach(editor.markupModel::removeHighlighter)
}
