package com.github.hamfer.bracketblock.highlighter.lang

import com.github.hamfer.bracketblock.highlighter.HighlightRange
import com.github.hamfer.bracketblock.settings.PluginSettings
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag

class XmlLanguageHighlighter : LanguageHighlighter {
    override fun id(): String = "XML"
    override fun displayName(): String = "XML"
    override fun options(): List<LanguageOption> =
        listOf(LanguageOption(XML_TAG, "XmlTag", true))

    override fun detect(editor: Editor, psiFile: PsiFile, offset: Int): HighlightRange? {
        val settings = PluginSettings.getInstance()
        if (!settings.isLanguageOptionEnabled(id(), XML_TAG, true)) return null
        val element = findElementSafely(psiFile, offset) ?: return null
        val tag = PsiTreeUtil.getParentOfType(element, XmlTag::class.java, false) ?: return null
        val range = tag.textRange
        return HighlightRange(
            startOffset = range.startOffset,
            endOffset = range.endOffset,
            bracketStartOffset = range.startOffset,
            bracketEndOffset = range.endOffset
        )
    }

    private fun findElementSafely(file: PsiFile, offset: Int): PsiElement? {
        val safe = offset.coerceIn(0, maxOf(0, file.textLength - 1))
        return file.findElementAt(safe)
    }

    companion object {
        private const val XML_TAG = "com.intellij.psi.xml.XmlTag"
    }
}
