package com.zenia.app.pdf

import com.zenia.app.model.DiarioEntrada
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PdfFilterUtils {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun filterEntries(
        entries: List<DiarioEntrada>,
        range: DateRange
    ): List<DiarioEntrada> {

        return when (range) {

            is DateRange.SingleDay -> {
                entries.filter {
                    val entryDate = parseDate(it.fecha)
                    entryDate == range.date
                }
            }

            is DateRange.Period -> {
                entries.filter {
                    val entryDate = parseDate(it.fecha)
                    entryDate != null &&
                            !entryDate.isBefore(range.start) &&
                            !entryDate.isAfter(range.end)
                }
            }
        }
    }

    private fun parseDate(dateString: String?): LocalDate? {
        return try {
            if (dateString.isNullOrEmpty()) null
            else LocalDate.parse(dateString, formatter)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}