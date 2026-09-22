package com.github.hamfer.bracketblock.highlighter

import com.github.hamfer.bracketblock.adapter.BraceMatchingUtilAdapter
import com.github.hamfer.bracketblock.brace.BracePair
import com.github.hamfer.bracketblock.brace.BraceTokenTypes
import com.github.hamfer.bracketblock.highlighter.lang.LanguageHighlighterRegistry
import com.github.hamfer.bracketblock.settings.PluginSettings
import com.intellij.lang.Language
import com.intellij.lang.LanguageBraceMatching
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import java.awt.Font
import java.util.*


class BracketBlockHighlighter(private val editor: Editor) {
    private var languageBracePairs: HashMap<String, List<Pair<IElementType, IElementType>>> = HashMap()

    private val psiFile: PsiFile?

    private var pluginSettings: PluginSettings = PluginSettings.getInstance()

    init {
        val languageList = Language.getRegisteredLanguages()
        psiFile = editor.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(editor.document) }
        for (language in languageList) {
            val pairedBraceMatcher = LanguageBraceMatching.INSTANCE.forLanguage(language)
            if (pairedBraceMatcher != null) {
                val bracePairs: Array<out com.intellij.lang.BracePair> = pairedBraceMatcher.pairs
                val braceList: MutableList<Pair<IElementType, IElementType>> = LinkedList()
                for (bracePair in bracePairs) {
                    val braceEntry: Pair<IElementType, IElementType> = Pair(
                        bracePair.leftBraceType, bracePair.rightBraceType
                    )
                    braceList.add(braceEntry)
                }
                languageBracePairs[language.id] = braceList
            }
        }
    }

    /**
     * Main entry point: finds the closest scope around the given offset.
     * Uses per-language highlighter when available, otherwise falls back to manual detection.
     */
    fun findClosetBracePairOrScope(offset: Int): HighlightRange? {
        // Try per-language PSI-based detection first
        if (psiFile != null) {
            if (offset <= 0 || offset >= psiFile.textLength) {
                return null
            }

            val langHighlighter = LanguageHighlighterRegistry.getFor(psiFile.language)
            if (langHighlighter != null) {
                if (!langHighlighter.isAvailable()) {
                    return null
                }
                val range = langHighlighter.detect(editor, psiFile, offset)
                if (range != null) return range
            }
        }

        // Fallback to manual bracket detection
        val bracePair = findClosetBracePair(offset)
        if (bracePair != null) {
            return HighlightRange(
                startOffset = bracePair.leftBrace.offset,
                endOffset = bracePair.rightBrace.offset,
                bracketStartOffset = bracePair.leftBrace.offset,
                bracketEndOffset = bracePair.rightBrace.offset
            )
        }

        return null
    }

    fun findClosetBracePair(offset: Int): BracePair? {
        val braceTokenBracePair: BracePair? = this.findClosetBracePairInBraceTokens(offset)
        val stringSymbolBracePair: BracePair? = this.findClosetBracePairInStringSymbols(offset)
        return if (braceTokenBracePair != null && stringSymbolBracePair != null) {
            if (offset - braceTokenBracePair.leftBrace.offset > offset - stringSymbolBracePair.leftBrace.offset && offset - braceTokenBracePair.rightBrace.offset < offset - stringSymbolBracePair.rightBrace.offset) {
                stringSymbolBracePair
            } else {
                braceTokenBracePair
            }
        } else {
            Optional.ofNullable(braceTokenBracePair).orElse(stringSymbolBracePair)
        }
    }

    /**
     * Highlights a scope range with the configured border color.
     */
    fun highlightScope(range: HighlightRange?): RangeHighlighter? {
        if (range == null) return null
        // Skip when brackets are on the same line if disabled in settings
        val doc = editor.document
        val startLine = doc.getLineNumber(range.startOffset)
        val endLine = doc.getLineNumber(range.endOffset.coerceAtMost(doc.textLength))
        if (pluginSettings.isOnlyHighlightWhenDiferrentLine() && startLine == endLine) return null

        val textAttribute = TextAttributes(
            /* foreground */ null,
            /* background */ pluginSettings.getBackgroundColor(),
            /* effect border */ pluginSettings.getBorderColor(),
            EffectType.ROUNDED_BOX,
            Font.PLAIN
        )
        return editor.markupModel.addRangeHighlighter(
            range.startOffset,
            range.endOffset,
            HighlighterLayer.ELEMENT_UNDER_CARET + BracketBlockConstant.HIGHLIGHT_LAYER_WEIGHT,
            textAttribute,
            HighlighterTargetArea.EXACT_RANGE
        )
    }

    fun clearHighlight(highlighterList: List<RangeHighlighter>) {
        highlighterList.forEach { editor.markupModel.removeHighlighter(it) }
    }

    private fun findClosetBracePairInBraceTokens(offset: Int): BracePair? {
        val editorHighlighter = (editor as EditorEx).highlighter
        val isBlockCaret = this.isBlockCaret()
        val braceTokens: List<Pair<IElementType, IElementType>>? = getSupportedBraceToken()
        if (braceTokens != null) {
            for ((first, second) in braceTokens) {
                val leftTraverseIterator = editorHighlighter.createIterator(offset)
                val rightTraverseIterator = editorHighlighter.createIterator(offset)
                val leftBraceOffset: Int = BraceMatchingUtilAdapter.findLeftLParen(
                    leftTraverseIterator,
                    first,
                    editor.document.immutableCharSequence,
                    psiFile?.fileType,
                    isBlockCaret
                )
                val rightBraceOffset: Int = BraceMatchingUtilAdapter.findRightRParen(
                    rightTraverseIterator,
                    second,
                    editor.document.immutableCharSequence,
                    psiFile?.fileType,
                    isBlockCaret
                )
                if (leftBraceOffset != BracketBlockConstant.NON_OFFSET && rightBraceOffset != BracketBlockConstant.NON_OFFSET) {
                    return BracePair.BracePairBuilder().leftType(first).rightType(second)
                        .leftOffset(leftBraceOffset).rightOffset(rightBraceOffset).build()
                }
            }
        }
        return null
    }

    private fun findClosetBracePairInStringSymbols(offset: Int): BracePair? {
        if (offset < 0 || editor.document.immutableCharSequence.isEmpty()) return null
        val editorHighlighter = (editor as EditorEx).highlighter
        val iterator = editorHighlighter.createIterator(offset)
        val type: IElementType = iterator.tokenType
        val isBlockCaret: Boolean = this.isBlockCaret()
        if (!BraceMatchingUtilAdapter.isStringToken(type)) return null
        val leftOffset = iterator.start
        val rightOffset = iterator.end
        return if (!isBlockCaret && leftOffset == offset) null else BracePair.BracePairBuilder()
            .leftType(BraceTokenTypes.DOUBLE_QUOTE).rightType(BraceTokenTypes.DOUBLE_QUOTE).leftOffset(leftOffset)
            .rightOffset(rightOffset).build()
    }

    private fun getSupportedBraceToken(): List<Pair<IElementType, IElementType>>? {
        return languageBracePairs[psiFile?.language?.id]
    }

    private fun isBlockCaret(): Boolean {
        return editor.settings.isBlockCursor
    }
}
