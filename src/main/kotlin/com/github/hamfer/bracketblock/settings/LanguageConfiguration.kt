package com.github.hamfer.bracketblock.settings

/** How a language's structural scope is discovered. */
enum class ScopeKind {
    /** A paired delimiter is supplied by the language PSI or brace matcher. */
    BRACKET,

    /** The scope is delimited by indentation rather than a closing token. */
    INDENT,

    /** The scope is represented by a markup element and its matching tag. */
    MARKUP
}

/**
 * Configuration for one IntelliJ language.
 *
 * [languageId] must be the IntelliJ language id (for example, `Kotlin` or
 * `HTML`). [opening] and [closing] contain the delimiter characters used by
 * bracket and markup configurations. They are intentionally strings so a
 * language can configure more than one delimiter pair. For [INDENT], both
 * values are normally empty.
 */
data class LanguageConfiguration(
    var languageId: String = "",
    var kind: ScopeKind = ScopeKind.BRACKET,
    var opening: String = "({[",
    var closing: String = ")]}",
    var highlightScope: Boolean = true,
    var highlightBrackets: Boolean = true
) {
    init {
        languageId = languageId.trim()
        require(opening.length == closing.length || kind == ScopeKind.INDENT) {
            "Opening and closing delimiter lists must have the same length"
        }
    }

    /** Returns the configured delimiter pairs in matching order. */
    fun delimiterPairs(): List<Pair<Char, Char>> =
        opening.zip(closing)

    /** Whether this configuration can be used for the supplied language id. */
    fun appliesTo(id: String): Boolean = languageId.equals(id, ignoreCase = true)
}
