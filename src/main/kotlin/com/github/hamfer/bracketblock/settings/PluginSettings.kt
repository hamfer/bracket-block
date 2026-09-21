package com.github.hamfer.bracketblock.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.awt.Color

@Service
@State(name = "BracketBlockSetting", storages = [Storage("bracket-block.xml")])
class PluginSettings private constructor() : PersistentStateComponent<PluginSettingsState> {
    companion object { fun getInstance(): PluginSettings = ApplicationManager.getApplication().getService(PluginSettings::class.java) }
    private var state = PluginSettingsState()
    override fun getState() = state
    override fun loadState(state: PluginSettingsState) { this.state = state }
    fun setBorderColor(color: Color) { state.borderColor = color.rgb }
    fun getBorderColor() = Color((state.borderColor shr 16) and 0xFF, (state.borderColor shr 8) and 0xFF, state.borderColor and 0xFF)
    fun isHighlightScope() = state.highlightScope
    fun isHighlightBrackets() = state.highlightBrackets
    fun configurations(): List<LanguageConfiguration> = state.languageConfigurations
    fun configurationFor(languageId: String): LanguageConfiguration? = configurations().firstOrNull { it.languageId.equals(languageId, true) }
}
