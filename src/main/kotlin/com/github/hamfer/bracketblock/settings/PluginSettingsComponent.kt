package com.github.hamfer.bracketblock.settings

import com.github.hamfer.bracketblock.highlighter.lang.LanguageHighlighterRegistry
import com.intellij.ui.ColorPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.jetbrains.annotations.NotNull
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel


class PluginSettingsComponent {
    private var mainPanel: JPanel? = null

    // General
    private val borderColor = ColorPanel()
    private val backgroundColor = ColorPanel()
    private val borderWidthSpinner = JSpinner(SpinnerNumberModel(1.5, 0.5, 12.0, 0.5))
    private val onlyHighlightWhenDifferentLine = JBCheckBox("Only highlight when bracket on different line")
    private val borderOpacitySpinner = JSpinner(SpinnerNumberModel(100, 0, 100, 1))
    private val backgroundOpacitySpinner = JSpinner(SpinnerNumberModel(20, 0, 100, 1))

    // Per-language options: languageId -> optionKey -> checkbox
    private val languageOptionCheckboxes: MutableMap<String, MutableMap<String, JBCheckBox>> = mutableMapOf()

    init {
        val settings = PluginSettings.getInstance()
        borderColor.selectedColor = settings.getBorderColor()
        backgroundColor.selectedColor = settings.getBackgroundColor()
        onlyHighlightWhenDifferentLine.isSelected = settings.isOnlyHighlightWhenDiferrentLine()
        borderOpacitySpinner.value = settings.getBorderOpacityPercent()
        backgroundOpacitySpinner.value = settings.getBackgroundOpacityPercent()

        val fb = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Border color:"), borderColor, 1, false)
            .addLabeledComponent(JBLabel("Background color:"), backgroundColor, 1, false)
            .addLabeledComponent(JBLabel("Border opacity (%):"), borderOpacitySpinner as JComponent, 1, false)
            .addLabeledComponent(JBLabel("Background opacity (%):"), backgroundOpacitySpinner as JComponent, 1, false)
            .addLabeledComponent(JBLabel("Border width (px):"), borderWidthSpinner as JComponent, 1, false)
            .addComponent(onlyHighlightWhenDifferentLine)

        // Dynamic language sections
        val entries = LanguageHighlighterRegistry.availableEntries().entries
        for ((languageId, highlighter) in entries) {
            val options = highlighter.options()
            if (options.isEmpty()) continue
            fb.addComponent(JBLabel(highlighter.displayName()))
            val map: MutableMap<String, JBCheckBox> = mutableMapOf()
            for (opt in options) {
                val enabled = settings.isLanguageOptionEnabled(languageId, opt.key, opt.defaultEnabled)
                val cb = JBCheckBox(opt.label, enabled)
                map[opt.key] = cb
                fb.addComponent(cb)
            }
            languageOptionCheckboxes[languageId] = map
        }

        mainPanel = fb.addComponentFillVertically(JPanel(), 0).panel
    }

    fun getPanel(): JPanel? {
        return mainPanel
    }

    @NotNull
    fun getBorderColor(): Color = borderColor.selectedColor ?: JBColor.WHITE

    @NotNull
    fun getBackgroundColor(): Color = backgroundColor.selectedColor ?: JBColor.WHITE

    fun getBorderOpacityPercent(): Int = (borderOpacitySpinner.value as Number).toInt()

    fun getBackgroundOpacityPercent(): Int = (backgroundOpacitySpinner.value as Number).toInt()

    fun isOnlyHighlightWhenDifferentLine(): Boolean = onlyHighlightWhenDifferentLine.isSelected

    fun getLanguageOptionStates(): Map<String, Map<String, Boolean>> =
        languageOptionCheckboxes.mapValues { (_, m) -> m.mapValues { it.value.isSelected } }
}