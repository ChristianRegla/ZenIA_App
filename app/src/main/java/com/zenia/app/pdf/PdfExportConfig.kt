package com.zenia.app.pdf

import java.time.LocalDate

data class PdfExportConfig(
    val includeMood: Boolean = true,
    val includeActivities: Boolean = true,
    val includeNotes: Boolean = true,
    val includeSmartwatchData: Boolean = false,
    val includeLogo: Boolean = true,
    val dateRange: DateRange
)

sealed class DateRange {
    data class SingleDay(val date: LocalDate) : DateRange()
    data class Period(val start: LocalDate, val end: LocalDate) : DateRange()
}