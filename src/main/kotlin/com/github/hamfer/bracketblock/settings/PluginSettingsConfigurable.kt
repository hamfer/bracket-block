package com.github.hamfer.bracketblock.settings

import com.intellij.openapi.options.Configurable
import com.github.hamfer.bracketblock.highlighter.lang.LanguageHighlighterRegistry
import javax.swing.JComponent

class PluginSettingsConfigurable : Configurable {
    private var pluginSettingsComponent: PluginSettingsComponent? = null

    override fun createComponent(): JComponent? {
        pluginSettingsComponent = PluginSettingsComponent()
        return pluginSettingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        val settings = PluginSettings.getInstance()
        val comp = pluginSettingsComponent ?: return false
        if (comp.getBorderColor() != settings.getBorderColor()) return true
        if (comp.getBackgroundColor() != settings.getBackgroundColor()) return true
        if (comp.getBorderOpacityPercent() != settings.getBorderOpacityPercent()) return true
        if (comp.getBackgroundOpacityPercent() != settings.getBackgroundOpacityPercent()) return true
        if (comp.isOnlyHighlightWhenDifferentLine() != settings.isOnlyHighlightWhenDiferrentLine()) return true
        // Compare per-language options
        val states = comp.getLanguageOptionStates()
        for ((langId, highlighter) in LanguageHighlighterRegistry.availableEntries()) {
            for ((key, _, defaultEnabled) in highlighter.options()) {
                val selected = states[langId]?.get(key)
                val current = settings.isLanguageOptionEnabled(langId, key, defaultEnabled)
                if (selected != null && selected != current) return true
            }
        }
        return false
    }

    override fun apply() {
        val settings = PluginSettings.getInstance()
        val comp = pluginSettingsComponent ?: return
        settings.setBorderColor(comp.getBorderColor())
        settings.setBackgroundColor(comp.getBackgroundColor())
        settings.setBorderOpacityPercent(comp.getBorderOpacityPercent())
        settings.setBackgroundOpacityPercent(comp.getBackgroundOpacityPercent())
        settings.setOnlyHighlightWhenDifferentLine(comp.isOnlyHighlightWhenDifferentLine())
        val states = comp.getLanguageOptionStates()
        for ((langId, opts) in states) {
            for ((key, enabled) in opts) {
                settings.setLanguageOptionEnabled(langId, key, enabled)
            }
        }
    }

    override fun getDisplayName(): String {
        return "Bracket Block"
    }
}