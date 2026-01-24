package com.zenia.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.zenia.app.model.DiarioEntrada
import androidx.core.graphics.scale
import com.zenia.app.R
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

object PdfGenerator {

    fun generateDiaryPdf(
        context: Context,
        entries: List<DiarioEntrada>,
        userName: String,
        includeLogo: Boolean
    ): android.net.Uri? {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        val pageWidth = 595
        val pageHeight = 842
        var myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var myPage = pdfDocument.startPage(myPageInfo)
        var canvas = myPage.canvas

        var yPosition = 60f
        val startX = 40f
        val endX = pageWidth - 40f

        if (includeLogo) {
            val logoBitmap = getBitmapFromDrawable(context, R.mipmap.ic_launcher)

            if (logoBitmap != null) {
                val scaledLogo = logoBitmap.scale(50, 50, false)
                canvas.drawBitmap(scaledLogo, startX, 40f, paint)
            }
        }

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f
        titlePaint.color = Color.BLACK
        canvas.drawText("Reporte de Bienestar ZenIA", if (includeLogo) 100f else startX, 65f, titlePaint)

        paint.textSize = 12f
        paint.color = Color.GRAY
        canvas.drawText("Generado para: $userName", if (includeLogo) 100f else startX, 85f, paint)

        yPosition += 80f

        val bodyPaint = Paint()
        bodyPaint.textSize = 12f

        for (entry in entries) {
            if (yPosition > pageHeight - 100) {
                pdfDocument.finishPage(myPage)
                myPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.pages.size + 1).create()
                myPage = pdfDocument.startPage(myPageInfo)
                canvas = myPage.canvas
                yPosition = 60f
            }

            titlePaint.textSize = 14f
            canvas.drawText("📅 ${entry.fecha}  |  Ánimo: ${entry.estadoAnimo ?: "-"}", startX, yPosition, titlePaint)
            yPosition += 20f

            if (entry.actividades.isNotEmpty()) {
                bodyPaint.color = Color.DKGRAY
                canvas.drawText("Actividades: ${entry.actividades.joinToString(", ")}", startX, yPosition, bodyPaint)
                yPosition += 15f
            }

            if (entry.notas.isNotEmpty()) {
                bodyPaint.color = Color.BLACK
                val notePrefix = "Nota: "
                val maxChars = 80
                val text = notePrefix + entry.notas

                val lines = text.chunked(maxChars)
                for (line in lines) {
                    canvas.drawText(line, startX, yPosition, bodyPaint)
                    yPosition += 15f
                }
            }

            yPosition += 10f
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(startX, yPosition, endX, yPosition, paint)
            yPosition += 30f
        }

        pdfDocument.finishPage(myPage)

        val folder = File(context.cacheDir, "pdfs")
        if (!folder.exists()) folder.mkdirs()
        folder.listFiles()?.forEach { it.delete() }
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

    private fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

        val bitmap = createBitmap(drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 1)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}