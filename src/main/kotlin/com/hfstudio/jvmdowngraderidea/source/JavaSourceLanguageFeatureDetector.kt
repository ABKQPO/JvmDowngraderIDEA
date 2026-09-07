package com.hfstudio.jvmdowngraderidea.source

object JavaSourceLanguageFeatureDetector {
    private val localVar = Regex("\\bvar\\s+[A-Za-z_$][\\w$]*\\s*(?==|:|;|,|\\))")
    private val varLambdaParameter = Regex("\\(\\s*var\\s+[A-Za-z_$][\\w$]*")
    private val recordDeclaration = Regex("\\brecord\\s+[A-Za-z_$][\\w$]*\\s*\\(")
    private val sealedDeclaration = Regex("\\b(?:sealed|non-sealed)\\s+(?:class|interface)\\b")
    private val instanceOfPattern = Regex(
        "\\binstanceof\\s+[A-Za-z_$][\\w$]*(?:\\s*<[^>{};()]*>)?(?:\\s*\\[\\])?\\s+[A-Za-z_$][\\w$]*\\b",
    )
    private val switchRule = Regex("\\b(?:case|default)\\b[^{};]*->")
    private val patternSwitchRule = Regex(
        "\\bcase\\s+[A-Za-z_$][\\w$]*(?:\\s*<[^>{};()]*>)?(?:\\s*\\[\\])?\\s+[A-Za-z_$][\\w$]*\\s*(?:when\\s+[^{};]+)?->",
    )
    private val stringTemplate = Regex("\\b(?:STR|RAW)\\.\\\"")

    /**
     * Returns the minimum final Java release needed to parse syntax found in [source].
     * `null` means the supported syntax is Java 8-compatible.
     */
    fun detect(source: CharSequence): Int? {
        val sanitized = sanitize(source)
        val levels = buildList {
            if (sanitized.hasTextBlock) add(15)
            if (localVar.containsMatchIn(sanitized.code)) add(10)
            if (varLambdaParameter.containsMatchIn(sanitized.code)) add(11)
            if (switchRule.containsMatchIn(sanitized.code)) add(14)
            if (recordDeclaration.containsMatchIn(sanitized.code)) add(16)
            if (instanceOfPattern.containsMatchIn(sanitized.code)) add(16)
            if (sealedDeclaration.containsMatchIn(sanitized.code)) add(17)
            if (patternSwitchRule.containsMatchIn(sanitized.code)) add(21)
            if (stringTemplate.containsMatchIn(sanitized.code)) add(22)
        }
        return levels.maxOrNull()
    }

    private fun sanitize(source: CharSequence): SanitizedSource {
        val code = StringBuilder(source.length)
        var hasTextBlock = false
        var index = 0

        fun appendIgnored(character: Char) {
            code.append(if (character == '\n' || character == '\r') character else ' ')
        }

        while (index < source.length) {
            when {
                source.startsWith("//", index) -> {
                    while (index < source.length && source[index] != '\n') appendIgnored(source[index++])
                }

                source.startsWith("/*", index) -> {
                    appendIgnored(source[index++])
                    appendIgnored(source[index++])
                    while (index < source.length && !source.startsWith("*/", index)) appendIgnored(source[index++])
                    if (index < source.length) {
                        appendIgnored(source[index++])
                        appendIgnored(source[index++])
                    }
                }

                source.startsWith("\\\"\\\"\\\"", index) -> {
                    hasTextBlock = true
                    repeat(3) { appendIgnored(source[index++]) }
                    while (index < source.length && !source.startsWith("\\\"\\\"\\\"", index)) {
                        appendIgnored(source[index++])
                    }
                    if (index < source.length) repeat(3) { appendIgnored(source[index++]) }
                }

                source[index] == '\"' || source[index] == '\'' -> {
                    val delimiter = source[index]
                    appendIgnored(source[index++])
                    while (index < source.length) {
                        val character = source[index]
                        appendIgnored(character)
                        index++
                        if (character == '\\' && index < source.length) {
                            appendIgnored(source[index++])
                        } else if (character == delimiter) {
                            break
                        }
                    }
                }

                else -> code.append(source[index++])
            }
        }
        return SanitizedSource(code, hasTextBlock)
    }

    private data class SanitizedSource(val code: CharSequence, val hasTextBlock: Boolean)
}
