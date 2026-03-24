package com.zenia.app.util

import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object ChartUtils {
    fun mapMoodToValue(mood: String?): Float {
        return when (mood?.lowercase()?.trim()) {
            "increíble", "radiactivo", "excelente", "5", "feliz", "alegre", "muy bien" -> 5f
            "bien", "contento", "4", "energético", "claridad", "descansado" -> 4f
            "normal", "neutral", "regular", "3", "tranquilidad", "ligero" -> 3f
            "mal", "triste", "cansado", "2", "desanimado", "sin motivación" -> 2f
            "terrible", "pésimo", "enojado", "1", "estresado" -> 1f
            else -> 0f
        }
    }

    val moodValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        when (value.toInt()) {
            5 -> "😄"
            4 -> "🙂"
            3 -> "😐"
            2 -> "🙁"
            1 -> "😭"
            else -> ""
        }
    }

    val dateAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        try {
            val date = LocalDate.ofEpochDay(value.toLong())
            val formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun dynamicDateAxisFormatter(rangeDays: Int) = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        try {
            val date = LocalDate.ofEpochDay(value.toLong())
            val spanishLocale = Locale.forLanguageTag("es-ES")

            if (rangeDays <= 7) {
                val formatter = DateTimeFormatter.ofPattern("EEE", spanishLocale)
                formatter.format(date).replaceFirstChar { it.uppercase() }
            } else {
                val formatter = DateTimeFormatter.ofPattern("dd MMM", spanishLocale)
                formatter.format(date)
            }
        } catch (e: Exception) {
            ""
        }
    }
}