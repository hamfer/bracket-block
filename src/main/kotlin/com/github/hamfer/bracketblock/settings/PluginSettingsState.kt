package com.github.hamfer.bracketblock.settings

import java.awt.Color

/**
 * Persistent plugin settings state.
 */
data class PluginSettingsState(
    // General appearance
    var borderColor: Int = Color.WHITE.rgb,
    var borderOpacityPercent: Int = 100,

    var backgroundColor: Int = 0,
    // Opacity in percentage [0..100]
    var backgroundOpacityPercent: Int = 20,

    var onlyHighlightWhenDifferentLine: Boolean = false,
    // Per-language options: languageId -> (optionKey -> enabled)
    var languageOptions: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf()
)