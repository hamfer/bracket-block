package com.github.hamfer.bracketblock.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.awt.Color

@Service
@State(
    name = "BracketBlockSetting", storages = [Storage("bracket-block.xml")]

)
class PluginSettings private constructor() : PersistentStateComponent<PluginSettingsState> {
    companion object {
        fun getInstance(): PluginSettings {
            return ApplicationManager.getApplication().getService(PluginSettings::class.java)
        }
    }

    private var state = PluginSettingsState()

    override fun getState(): PluginSettingsState {
        return this.state
    }

    override fun loadState(state: PluginSettingsState) {
        this.state = state
    }

    fun setBorderColor(color: Color) {
        setBorderColor(color.rgb)
    }

    fun setBorderColor(color: Int) {
        state.borderColor = color
    }

    fun getBorderColor(): Color {
        val rgb = state.borderColor
        val alpha = percentToAlpha(state.borderOpacityPercent)
        return colorFromRgbInt(rgb, alpha)
    }

    fun setBackgroundColor(color: Color) {
        setBackgroundColor(color.rgb)
    }

    fun setBackgroundColor(color: Int) {
        state.backgroundColor = color
    }

    fun getBackgroundColor(): Color {
        val rgb = state.backgroundColor
        val alpha = percentToAlpha(state.backgroundOpacityPercent)
        return colorFromRgbInt(rgb, alpha)
    }

    fun setBorderOpacityPercent(percent: Int) {
        state.borderOpacityPercent = percent.coerceIn(0, 100)
    }

    fun getBorderOpacityPercent(): Int = state.borderOpacityPercent.coerceIn(0, 100)

    fun setBackgroundOpacityPercent(percent: Int) {
        state.backgroundOpacityPercent = percent.coerceIn(0, 100)
    }

    fun getBackgroundOpacityPercent(): Int = state.backgroundOpacityPercent.coerceIn(0, 100)

    fun setOnlyHighlightWhenDifferentLine(enabled: Boolean) {
        state.onlyHighlightWhenDifferentLine = enabled
    }

    fun isOnlyHighlightWhenDiferrentLine(): Boolean = state.onlyHighlightWhenDifferentLine

    /** Per-language option flags */
    fun isLanguageOptionEnabled(languageId: String, optionKey: String, default: Boolean = true): Boolean {
        val langMap = state.languageOptions[languageId] ?: return default
        return langMap[optionKey] ?: default
    }

    fun setLanguageOptionEnabled(languageId: String, optionKey: String, enabled: Boolean) {
        val langMap = state.languageOptions.getOrPut(languageId) { mutableMapOf() }
        langMap[optionKey] = enabled
    }

    fun getLanguageOptions(languageId: String): Map<String, Boolean> =
        state.languageOptions[languageId] ?: emptyMap()

    private fun colorFromRgbInt(rgb: Int, alpha: Int): Color {
        return Color(
            (rgb shr 16) and 0xFF,
            (rgb shr 8) and 0xFF,
            rgb and 0xFF,
            alpha.coerceIn(0, 255)
        )
    }

    private fun percentToAlpha(percent: Int): Int = (percent.coerceIn(0, 100) * 255 / 100)
}