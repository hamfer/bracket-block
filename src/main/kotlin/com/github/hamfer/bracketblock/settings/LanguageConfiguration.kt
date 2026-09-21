package com.github.hamfer.bracketblock.settings

/** How a language's structural scope is discovered. */
enum ScopeKind { BRACKET, INDENT, MARKUP }

data class LanguageConfiguration(
    var languageId: String = "",
    var kind: ScopeKind = ScopeKind.BRACKET,
    var opening: String = "({[",
    var closing: String = ")}]",