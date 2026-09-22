package com.github.hamfer.bracketblock.highlighter.lang

import com.intellij.lang.Language

/**
 * Registry that maps IntelliJ language IDs to LanguageHighlighter implementations.
 */
object LanguageHighlighterRegistry {
    private val byId: MutableMap<String, LanguageHighlighter> = mutableMapOf()

    init {
        // Register built-ins we support out of the box
        // HTML and XML
        register("HTML", HtmlLanguageHighlighter())
        register("XML", XmlLanguageHighlighter())
        // Java
        register("JAVA", JavaLanguageHighlighter())
        // Kotlin
        register("kotlin", KotlinLanguageHighlighter())
        // JavaScript / TypeScript (share implementation)
        val js = JavaScriptLanguageHighlighter()
        register("JavaScript", js)
        register("TypeScript", js)
        // Python
        register("Python", PythonLanguageHighlighter())
        // YAML
        register("yaml", YamlLanguageHighlighter())
        // JSON
        register("JSON", JsonLanguageHighlighter())
        // CSS
        register("CSS", CssLanguageHighlighter())
        // More languages can be added with their own classes
    }

    fun register(languageId: String, highlighter: LanguageHighlighter) {
        byId[languageId] = highlighter
    }

    fun get(languageId: String): LanguageHighlighter? {
        val h = byId[languageId]
        return h
    }

    fun getFor(language: Language): LanguageHighlighter? = get(language.id)

    /** Returns available highlighters (filtered by isAvailable) as id->instance map. */
    fun availableEntries(): Map<String, LanguageHighlighter> =
        byId.filterValues { it.isAvailable() }
}
