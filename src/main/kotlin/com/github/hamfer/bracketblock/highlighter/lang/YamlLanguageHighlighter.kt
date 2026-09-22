package com.github.hamfer.bracketblock.highlighter.lang

import com.github.hamfer.bracketblock.highlighter.HighlightRange
import com.github.hamfer.bracketblock.settings.PluginSettings
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

class YamlLanguageHighlighter : LanguageHighlighter {
    override fun id(): String = "yaml"
    override fun displayName(): String = "YAML"

    private val classNames = listOf(
        "org.jetbrains.yaml.psi.YAMLBlockMapping",
        "org.jetbrains.yaml.psi.YAMLBlockSequence",
        "org.jetbrains.yaml.psi.YAMLDocument"
    )

    private val classes: List<Pair<String, Class<*>?>> by lazy { classNames.map { it to resolve(it) } }

    override fun options(): List<LanguageOption> =
        classNames.map { cls -> LanguageOption(cls, cls.substringAfterLast('.'), true) }

    override fun isAvailable(): Boolean = classes.any { it.second != null }

    override fun detect(editor: Editor, psiFile: PsiFile, offset: Int): HighlightRange? {
        val element = findElementSafely(psiFile, offset) ?: return null
        val enabled = enabledClasses()
        if (enabled.isEmpty()) return null
        var current: PsiElement? = element
        var best: HighlightRange? = null
        while (current != null && current !is PsiFile) {
            if (matches(current, enabled)) {
                val r = current.textRange
                val range = HighlightRange(
                    startOffset = r.startOffset,
                    endOffset = r.endOffset,
                    bracketStartOffset = r.startOffset,
                    bracketEndOffset = r.endOffset
                )
                if (best == null || (range.endOffset - range.startOffset) < (best.endOffset - best.startOffset)) {
                    best = range
                }
            }
            current = current.parent
        }
        return best
    }

    private fun enabledClasses(): List<Class<*>> {
        val settings = PluginSettings.getInstance()
        return classes.mapNotNull { (key, cls) ->
            val c = cls ?: return@mapNotNull null
            if (settings.isLanguageOptionEnabled(id(), key, true)) c else null
        }
    }

    private fun matches(element: PsiElement, enabled: List<Class<*>>): Boolean = enabled.any { it.isInstance(element) }

    private fun resolve(name: String): Class<*>? = try {
        Class.forName(name, false, this::class.java.classLoader)
    } catch (_: Throwable) { null }

    private fun findElementSafely(file: PsiFile, offset: Int): PsiElement? {
        val safe = offset.coerceIn(0, maxOf(0, file.textLength - 1))
        return file.findElementAt(safe)
    }
}
