package com.zenia.app.util

import android.content.Context
import com.zenia.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Normalizer
import kotlin.math.abs

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

    private val blockedWordsMap = HashMap<String, BadWord>()
    private val blockedWordsList = ArrayList<BadWord>()

    private val blockedWords = HashSet<BadWord>()

    private val hardcodedDefaults = listOf(
        BadWord("puto", Severity.HIGH),
        BadWord("puta", Severity.HIGH),
    )

    init {
        blockedWords.addAll(hardcodedDefaults)
    }

    private val leetMap = mapOf(
        '0' to 'o', '1' to 'i', '3' to 'e', '4' to 'a', '5' to 's', '7' to 't', '@' to 'a'
    )

    private val attackPatterns = listOf(
        "eres", "sos", "tu eres", "you are", "u are"
    )

    private const val BLOCK_THRESHOLD = 5

    suspend fun loadFromCsv(context: Context) = withContext(Dispatchers.IO) {
        hardcodedDefaults.forEach {
            blockedWordsMap[it.word] = it
            blockedWordsList.add(it)
        }

        val files = listOf(R.raw.profanity_en)

        files.forEach { resId ->
            try {
                context.resources.openRawResource(resId).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readLine()
                        reader.forEachLine { line ->
                            val tokens = line.split(",")
                            if (tokens.isNotEmpty()) {
                                val wordRaw = tokens[0].trim()
                                if (wordRaw.isNotBlank()) {
                                    val wordNorm = normalize(wordRaw)
                                    val severity = when (tokens.lastOrNull()?.trim()) {
                                        "Severe", "High" -> Severity.HIGH
                                        "Strong", "Medium" -> Severity.MEDIUM
                                        else -> Severity.LOW
                                    }
                                    val badWord = BadWord(wordNorm, severity)
                                    blockedWordsMap[wordNorm] = badWord
                                    blockedWordsList.add(badWord)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun hasProfanity(input: String): Boolean {
        return analyze(input).score >= BLOCK_THRESHOLD
    }

    fun analyze(input: String): ProfanityResult {
        if (input.isBlank()) return ProfanityResult(0, emptySet())

        val normalized = normalize(input)
        val inputWords = normalized.split(" ")

        var score = 0
        val detected = mutableSetOf<String>()

        for (word in inputWords) {
            blockedWordsMap[word]?.let { bad ->
                score += bad.severity.weight + 2
                detected.add(bad.word)
            }
        }

        for (word in inputWords) {
            if (detected.contains(word)) continue

            val candidates = blockedWordsList.filter {
                abs(it.word.length - word.length) <= 1
            }

            for (bad in candidates) {
                if (levenshtein(word, bad.word) <= 1) {
                    score += bad.severity.weight
                    detected.add(bad.word)
                }
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
        if (abs(a.length - b.length) > 1) return 2

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