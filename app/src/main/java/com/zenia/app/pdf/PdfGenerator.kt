package com.zenia.app.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import com.zenia.app.R
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.data.HealthSummary
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

object PdfGenerator {
    fun generateDiaryPdf(
        context: Context,
        entries: List<DiarioEntrada>,
        smartwatchData: List<HealthSummary>?,
        userName: String,
        config: PdfExportConfig
    ): android.net.Uri? {

        val filteredEntries = PdfFilterUtils.filterEntries(entries, config.dateRange)

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = 60f
        val startX = 40f
        val endX = pageWidth - 40f

        val titlePaint = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }

        val bodyPaint = Paint().apply {
            textSize = 12f
            color = Color.DKGRAY
        }

        // LOGO
        if (config.includeLogo) {
            val logo = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            logo?.let {
                val bitmap = createBitmap(it.intrinsicWidth.takeIf { w -> w > 0 } ?: 1,
                    it.intrinsicHeight.takeIf { h -> h > 0 } ?: 1)
                val canvasBitmap = Canvas(bitmap)
                it.setBounds(0, 0, canvasBitmap.width, canvasBitmap.height)
                it.draw(canvasBitmap)
                val scaled = bitmap.scale(50, 50)
                canvas.drawBitmap(scaled, startX, 30f, null)
            }
        }

        canvas.drawText(
            "Reporte de Bienestar ZenIA",
            if (config.includeLogo) 100f else startX,
            60f,
            titlePaint
        )

        canvas.drawText(
            "Generado para: $userName",
            if (config.includeLogo) 100f else startX,
            80f,
            bodyPaint
        )

        y += 100f

        // ENTRADAS
        for (entry in filteredEntries) {

            if (y > pageHeight - 120) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            titlePaint.textSize = 14f
            canvas.drawText("📅 ${entry.fecha}", startX, y, titlePaint)
            y += 20f

            if (config.includeMood) {
                canvas.drawText("Ánimo: ${entry.estadoAnimo ?: "-"}", startX, y, bodyPaint)
                y += 18f
            }

            if (config.includeActivities && entry.actividades.isNotEmpty()) {
                canvas.drawText(
                    "Actividades: ${entry.actividades.joinToString(", ")}",
                    startX,
                    y,
                    bodyPaint
                )
                y += 18f
            }

            if (config.includeNotes && entry.notas.isNotEmpty()) {
                y = drawWrappedText(
                    canvas,
                    entry.notas,
                    startX,
                    y,
                    endX,
                    bodyPaint
                )
            }

            y += 20f
        }

        // SMARTWATCH (PREMIUM)
        if (config.includeSmartwatchData && smartwatchData != null) {

            if (y > pageHeight - 150) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            titlePaint.textSize = 16f
            canvas.drawText("Datos de Salud (Smartwatch)", startX, y, titlePaint)
            y += 25f

            smartwatchData.forEach { data ->
                canvas.drawText(
                    "FC Promedio: ${data.heartRateAvg ?: "--"} bpm | Sueño: ${data.sleepHours} h | Pasos: ${data.steps}",
                    startX,
                    y,
                    bodyPaint
                )
                y += 18f
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

        val words = text.split(" ")
        var line = ""

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val width = paint.measureText(testLine)

            if (width > maxWidth) {
                canvas.drawText(line, startX, y, paint)
                y += 18f
                line = word
            } else {
                line = testLine
            }
        }

        if (line.isNotEmpty()) {
            canvas.drawText(line, startX, y, paint)
            y += 18f
        }

        return y
    }
}