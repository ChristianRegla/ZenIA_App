package com.zenia.app.util

import java.text.Normalizer

object ProfanityFilter {

    enum class Severity(val weight: Int) {
        LOW(1),
        MEDIUM(3),
        HIGH(5)
    }

    data class BadWord(
        val word: String,
        val severity: Severity
    )

    data class ProfanityResult(
        val score: Int,
        val detected: Set<String>
    )

    private val blockedWords = listOf(
        BadWord("puto", Severity.HIGH),
        BadWord("puta", Severity.HIGH),
        BadWord("mierda", Severity.LOW),
        BadWord("verga", Severity.MEDIUM),
        BadWord("pendejo", Severity.HIGH),
        BadWord("cabron", Severity.MEDIUM),
        BadWord("estupido", Severity.LOW),
        BadWord("idiota", Severity.LOW),
        BadWord("imbecil", Severity.LOW),
        BadWord("zorra", Severity.MEDIUM),
        BadWord("culo", Severity.LOW),
        BadWord("maricon", Severity.HIGH),
        BadWord("malparido", Severity.HIGH),

        BadWord("fuck", Severity.MEDIUM),
        BadWord("shit", Severity.LOW),
        BadWord("bitch", Severity.MEDIUM),
        BadWord("asshole", Severity.MEDIUM),
        BadWord("dick", Severity.MEDIUM),
        BadWord("pussy", Severity.MEDIUM),
        BadWord("bastard", Severity.MEDIUM),
        BadWord("whore", Severity.MEDIUM),
        BadWord("cunt", Severity.HIGH),
        BadWord("faggot", Severity.HIGH),
        BadWord("nigger", Severity.HIGH),
        BadWord("slut", Severity.MEDIUM)
    )

    private val leetMap = mapOf(
        '0' to 'o',
        '1' to 'i',
        '3' to 'e',
        '4' to 'a',
        '5' to 's',
        '7' to 't'
    )

    private val attackPatterns = listOf(
        "eres", "sos", "tu eres", "you are", "u are"
    )

    private const val BLOCK_THRESHOLD = 5

    fun hasProfanity(input: String): Boolean {
        return analyze(input).score >= BLOCK_THRESHOLD
    }

    fun analyze(input: String): ProfanityResult {
        if (input.isBlank()) return ProfanityResult(0, emptySet())

        val normalized = normalize(input)
        val words = normalized.split(" ")
        val compact = normalized.replace(" ", "")

        var score = 0
        val detected = mutableSetOf<String>()

        for (bad in blockedWords) {

            if (words.contains(bad.word)) {
                score += bad.severity.weight + 2
                detected.add(bad.word)
                continue
            }

            if (compact.contains(bad.word)) {
                score += bad.severity.weight + 1
                detected.add(bad.word)
                continue
            }

            if (words.any { levenshtein(it, bad.word) <= 1 }) {
                score += bad.severity.weight
                detected.add(bad.word)
            }
        }

        if (attackPatterns.any { normalized.contains(it) } && score > 0) {
            score += 2
        }

        return ProfanityResult(score, detected)
    }

    private fun normalize(text: String): String {
        val noAccents = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

        val leetReplaced = noAccents.map { leetMap[it] ?: it }.joinToString("")

        return leetReplaced
            .replace(Regex("[^a-z\\s]"), " ")
            .replace(Regex("(.)\\1{2,}"), "$1")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun levenshtein(a: String, b: String): Int {
        if (kotlin.math.abs(a.length - b.length) > 1) return 2

        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1
                )
            }
        }
        return dp[a.length][b.length]
    }
}