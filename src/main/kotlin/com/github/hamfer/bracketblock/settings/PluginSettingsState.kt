package com.github.hamfer.bracketblock.settings

import java.awt.Color

data class PluginSettingsState(
    var borderColor: Int = Color.WHITE.rgb,
    var highlightScope: Boolean = true,
    var highlightBrackets: Boolean = true,
    var languageConfigurations: MutableList<LanguageConfiguration> = defaultLanguageConfigurations()
) {
    companion object {
        fun defaultLanguageConfigurations(): MutableList<LanguageConfiguration> = mutableListOf(
            LanguageConfiguration("Java"), LanguageConfiguration("Kotlin"),
            LanguageConfiguration("JavaScript"), LanguageConfiguration("TypeScript"),
            LanguageConfiguration("Python", ScopeKind.INDENT, "", ""),
            LanguageConfiguration("HTML", ScopeKind.MARKUP, "<", ">"),
            LanguageConfiguration("XML", ScopeKind.MARKUP, "<", ">")
        )
    }
}
