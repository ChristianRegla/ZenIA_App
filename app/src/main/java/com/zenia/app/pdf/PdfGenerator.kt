package com.zenia.app.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import com.zenia.app.R
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.data.HealthSummary
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import androidx.core.graphics.createBitmap

object PdfGenerator {

    private val ZeniaTeal = "#0F766E".toColorInt()
    private val ZeniaSlateGrey = "#64748B".toColorInt()
    private val LightGrey = "#F1F5F9".toColorInt()
    private val CardBackground = "#F8FAFC".toColorInt()
    private val DividerColor = "#E2E8F0".toColorInt()

    fun generateDiaryPdf(
        context: Context,
        entries: List<DiarioEntrada>,
        smartwatchData: List<HealthSummary>?,
        userName: String,
        config: PdfExportConfig
    ): android.net.Uri? {

        val filteredAndSortedEntries = PdfFilterUtils.filterEntries(entries, config.dateRange)
            .sortedBy { entry ->
                try {
                    LocalDate.parse(entry.fecha)
                } catch (e: Exception) {
                    e.printStackTrace()
                    LocalDate.MIN
                }
            }

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val headerBgPaint = Paint().apply { color = LightGrey; style = Paint.Style.FILL }
        val titlePaint = Paint().apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = ZeniaTeal
        }
        val subtitlePaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = ZeniaSlateGrey
        }

        val dateTitlePaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = ZeniaTeal
        }
        val bodyPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.DKGRAY
        }
        val dividerPaint = Paint().apply {
            color = DividerColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val cardBgPaint = Paint().apply {
            color = CardBackground
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val cardBorderPaint = Paint().apply {
            color = DividerColor
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 120f, headerBgPaint)

        var currentY = 50f
        val startX = 40f
        val endX = pageWidth - 40f

        var textStartX = startX

        if (config.includeLogo) {
            val logo = ContextCompat.getDrawable(context, R.drawable.logo_zenia)
            logo?.let {
                val targetSize = 200
                val bitmap = createBitmap(targetSize, targetSize)
                val canvasBitmap = Canvas(bitmap)

                val path = Path()
                val radius = 40f
                path.addRoundRect(
                    RectF(0f, 0f, targetSize.toFloat(), targetSize.toFloat()),
                    radius, radius,
                    Path.Direction.CW
                )
                canvasBitmap.clipPath(path)

                it.setBounds(0, 0, canvasBitmap.width, canvasBitmap.height)
                it.draw(canvasBitmap)

                val destRect = RectF(startX, 30f, startX + 60f, 30f + 60f)
                val paintFilter = Paint(Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(bitmap, null, destRect, paintFilter)

                textStartX = startX + 80f
            }
        }

        canvas.drawText(context.getString(R.string.pdf_title), textStartX, currentY, titlePaint)
        currentY += 25f

        val localizedFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        val todayStr = LocalDate.now().format(localizedFormatter)

        val subtitleText = context.getString(R.string.pdf_subtitle, userName, todayStr)
        canvas.drawText(subtitleText, textStartX, currentY, subtitlePaint)

        currentY = 150f

        if (filteredAndSortedEntries.isEmpty()) {
            canvas.drawText(context.getString(R.string.pdf_no_records), startX, currentY, bodyPaint)
        } else {
            for (entry in filteredAndSortedEntries) {

                val entryDateStr = try {
                    LocalDate.parse(entry.fecha).format(localizedFormatter)
                } catch (e: Exception) {
                    e.printStackTrace()
                    entry.fecha
                }

                val moodMapped = if (config.includeMood && !entry.estadoAnimo.isNullOrBlank()) {
                    getMoodString(context, entry.estadoAnimo)
                } else null

                var entryHeight = 0f
                entryHeight += 35f
                entryHeight += 15f

                if (moodMapped != null) entryHeight += 20f
                if (config.includeActivities && entry.actividades.isNotEmpty()) entryHeight += 20f

                if (config.includeNotes && entry.notas.isNotBlank()) {
                    entryHeight += 20f
                    val textWidth = (endX - 16f) - (startX + 26f)
                    entryHeight += measureWrappedTextHeight(entry.notas, textWidth, bodyPaint)
                }
                entryHeight += 20f

                if (currentY + entryHeight > pageHeight - 50) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 60f
                }

                val cardRect = RectF(startX, currentY, endX, currentY + entryHeight)
                canvas.drawRoundRect(cardRect, 16f, 16f, cardBgPaint)
                canvas.drawRoundRect(cardRect, 16f, 16f, cardBorderPaint)

                var textY = currentY + 25f
                val innerStartX = startX + 16f
                val innerEndX = endX - 16f

                canvas.drawText(entryDateStr, innerStartX, textY, dateTitlePaint)
                textY += 12f

                canvas.drawLine(innerStartX, textY, innerEndX, textY, dividerPaint)
                textY += 20f

                if (moodMapped != null) {
                    val moodText = context.getString(R.string.pdf_mood_prefix, moodMapped)
                    canvas.drawText(moodText, innerStartX, textY, bodyPaint)
                    textY += 20f
                }

                if (config.includeActivities && entry.actividades.isNotEmpty()) {
                    val actText = context.getString(R.string.pdf_activities_prefix, entry.actividades.joinToString(", "))
                    canvas.drawText(actText, innerStartX, textY, bodyPaint)
                    textY += 20f
                }

                if (config.includeNotes && entry.notas.isNotBlank()) {
                    canvas.drawText(context.getString(R.string.pdf_notes_title), innerStartX, textY, subtitlePaint)
                    textY += 18f
                    textY = drawWrappedText(
                        canvas,
                        entry.notas,
                        innerStartX + 10f,
                        textY,
                        innerEndX,
                        bodyPaint
                    )
                }

                currentY += entryHeight + 24f
            }
        }

        if (config.includeSmartwatchData && !smartwatchData.isNullOrEmpty()) {

            var swHeight = 45f
            swHeight += smartwatchData.size * 25f
            swHeight += 15f

            if (currentY + swHeight > pageHeight - 50) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 60f
            }

            val swRect = RectF(startX, currentY, endX, currentY + swHeight)
            canvas.drawRoundRect(swRect, 16f, 16f, cardBgPaint)
            canvas.drawRoundRect(swRect, 16f, 16f, cardBorderPaint)

            var swY = currentY + 25f
            val innerStartX = startX + 16f

            canvas.drawText(context.getString(R.string.pdf_smartwatch_title), innerStartX, swY, dateTitlePaint)
            swY += 15f
            canvas.drawLine(innerStartX, swY, endX - 16f, swY, dividerPaint)
            swY += 20f

            smartwatchData.forEach { data ->
                val hr = data.heartRateAvg?.toString() ?: "--"
                val sleep = data.sleepHours.toString()
                val steps = data.steps.toString()

                val healthText = context.getString(R.string.pdf_smartwatch_data, hr, sleep, steps)
                canvas.drawText(healthText, innerStartX, swY, bodyPaint)
                swY += 25f
            }
        }

        pdfDocument.finishPage(page)

        val folder = File(context.cacheDir, "pdfs")
        if (!folder.exists()) folder.mkdirs()

        val file = File(folder, "Zenia_Report_${System.currentTimeMillis()}.pdf")

        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    private fun getMoodString(context: Context, moodVal: String?): String {
        return when (moodVal) {
            "1" -> context.getString(R.string.mood_1)
            "2" -> context.getString(R.string.mood_2)
            "3" -> context.getString(R.string.mood_3)
            "4" -> context.getString(R.string.mood_4)
            else -> moodVal ?: "-"
        }
    }

    private fun measureWrappedTextHeight(text: String, maxWidth: Float, paint: Paint): Float {
        var height = 0f
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var line = ""

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val width = paint.measureText(testLine)

                if (width > maxWidth) {
                    height += 16f
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                height += 16f
            }
            height += 4f
        }
        return height
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        startX: Float,
        startY: Float,
        endX: Float,
        paint: Paint
    ): Float {
        var y = startY
        val maxWidth = endX - startX

        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var line = ""

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val width = paint.measureText(testLine)

                if (width > maxWidth) {
                    canvas.drawText(line, startX, y, paint)
                    y += 16f
                    line = word
                } else {
                    line = testLine
                }
            }

            if (line.isNotEmpty()) {
                canvas.drawText(line, startX, y, paint)
                y += 16f
            }
            y += 4f
        }

        return y
    }
}