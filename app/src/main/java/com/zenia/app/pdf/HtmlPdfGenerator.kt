package com.zenia.app.pdf

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zenia.app.R
import com.zenia.app.data.HealthSummary
import com.zenia.app.model.DiarioEntrada
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object HtmlPdfGenerator {

    suspend fun generateDiaryPdfAsync(
        context: Context,
        entries: List<DiarioEntrada>,
        smartwatchData: List<HealthSummary>?,
        userName: String,
        config: PdfExportConfig
    ) = withContext(Dispatchers.Main) {

        val filteredEntries = PdfFilterUtils.filterEntries(entries, config.dateRange)
            .sortedBy {
                try { LocalDate.parse(it.fecha) } catch (e: Exception) { LocalDate.MIN }
            }

        val htmlContent = buildHtmlTemplate(context, filteredEntries, smartwatchData, userName, config)

        // Usamos un WebView invisible
        val webView = WebView(context)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                createWebPrintJob(context, view)
            }
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun createWebPrintJob(context: Context, webView: WebView) {
        // Obtenemos el servicio de impresión de Android
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "ZenIA_Report_${System.currentTimeMillis()}"
        val printAdapter = webView.createPrintDocumentAdapter(jobName)

        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .build()

        printManager.print(jobName, printAdapter, printAttributes)
    }

    private fun buildHtmlTemplate(
        context: Context,
        entries: List<DiarioEntrada>,
        swData: List<HealthSummary>?,
        userName: String,
        config: PdfExportConfig
    ): String {
        val localizedFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        val todayStr = LocalDate.now().format(localizedFormatter)

        val builder = StringBuilder()

        builder.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    @page { margin: 0; }
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #ffffff; color: #334155; margin: 0; padding: 0; }
                    .header { background-color: #F1F5F9; padding: 40px; text-align: left; border-bottom: 2px solid #0F766E; }
                    .title { color: #0F766E; font-size: 28px; font-weight: bold; margin: 0 0 10px 0; }
                    .subtitle { color: #64748B; font-size: 14px; margin: 0; }
                    .container { padding: 40px; }
                    .card { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; padding: 20px; margin-bottom: 24px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); page-break-inside: avoid; }
                    
                    .divider { border-top: 1px dashed #CBD5E1; margin: 16px 0; }
                    
                    .date-title { color: #0F766E; font-size: 16px; font-weight: bold; margin: 0; }
                    .mood-badge { display: inline-block; padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-bottom: 12px; }
                    
                    .card-mood-1 { border-left: 6px solid #EF4444; } .badge-mood-1 { background-color: #FEE2E2; color: #991B1B; }
                    .card-mood-2 { border-left: 6px solid #F59E0B; } .badge-mood-2 { background-color: #FEF3C7; color: #92400E; }
                    .card-mood-3 { border-left: 6px solid #3B82F6; } .badge-mood-3 { background-color: #DBEAFE; color: #1E40AF; }
                    .card-mood-4 { border-left: 6px solid #10B981; } .badge-mood-4 { background-color: #D1FAE5; color: #065F46; }
                    .card-mood-default { border-left: 6px solid #94A3B8; } .badge-mood-default { background-color: #F1F5F9; color: #475569; }

                    .section-title { font-size: 12px; color: #64748B; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px; }
                    .content-text { font-size: 14px; color: #334155; line-height: 1.5; margin: 0; }
                    .activities { background-color: #E2E8F0; padding: 4px 8px; border-radius: 6px; font-size: 12px; display: inline-block; margin-right: 6px; margin-bottom: 6px; }
                    
                    /* Estilos para las barras de progreso */
                    .progress-bg { background-color: #E2E8F0; border-radius: 4px; height: 8px; width: 100%; margin-top: 12px; }
                    .progress-fill { background-color: #0F766E; height: 100%; border-radius: 4px; }
                </style>
            </head>
            <body>
        """.trimIndent())

        // Encabezado
        builder.append("""
            <div class="header">
                <h1 class="title">${context.getString(R.string.pdf_title)}</h1>
                <p class="subtitle">${context.getString(R.string.pdf_subtitle, userName, todayStr)}</p>
            </div>
            <div class="container">
        """.trimIndent())

        if (entries.isEmpty()) {
            builder.append("<p class='content-text'>${context.getString(R.string.pdf_no_records)}</p>")
        } else {
            for (entry in entries) {
                val entryDateStr = try { LocalDate.parse(entry.fecha).format(localizedFormatter) } catch (e: Exception) { entry.fecha }
                val moodClassNum = entry.estadoAnimo ?: "default"

                builder.append("<div class='card card-mood-$moodClassNum'>")
                builder.append("<h2 class='date-title'>$entryDateStr</h2>")

                if (config.includeMood && !entry.estadoAnimo.isNullOrBlank()) {
                    val moodText = getMoodString(context, entry.estadoAnimo)
                    builder.append("<div class='divider'></div>")
                    builder.append("<span class='mood-badge badge-mood-$moodClassNum'>$moodText</span>")
                }

                if (config.includeActivities && entry.actividades.isNotEmpty()) {
                    builder.append("<div>")
                    entry.actividades.forEach { act ->
                        builder.append("<span class='activities'>$act</span>")
                    }
                    builder.append("</div>")
                }

                if (config.includeNotes && entry.notas.isNotBlank()) {
                    builder.append("<div class='divider'></div>")
                    builder.append("<p class='section-title'>${context.getString(R.string.pdf_notes_title)}</p>")
                    val formattedNotes = entry.notas.replace("\n", "<br>")
                    builder.append("<p class='content-text'>$formattedNotes</p>")
                }

                builder.append("</div>")
            }
        }

        if (config.includeSmartwatchData && !swData.isNullOrEmpty()) {
            builder.append("<div style='margin-top: 40px; page-break-before: auto;'>")
            builder.append("<h1 class='title' style='font-size: 22px; border-bottom: 2px solid #0F766E; padding-bottom: 8px;'>${context.getString(R.string.pdf_smartwatch_title)}</h1>")

            swData.forEach { data ->
                builder.append("<div class='card card-mood-default' style='margin-top: 16px;'>")

                val hr = data.heartRateAvg?.toString() ?: "--"
                val sleep = data.sleepHours.toString()
                val steps = data.steps

                val healthText = context.getString(R.string.pdf_smartwatch_data, hr, sleep, steps.toString())
                builder.append("<p class='content-text'>$healthText</p>")

                val goal = 10000f
                val progressPercent = (steps.toFloat() / goal * 100f).coerceAtMost(100f).toInt()

                builder.append("""
                    <div class='progress-bg'>
                        <div class='progress-fill' style='width: ${progressPercent}%;'></div>
                    </div>
                    <p style='font-size: 11px; color: #64748B; margin-top: 6px; text-align: right;'>$steps / 10,000 pasos</p>
                """.trimIndent())

                builder.append("</div>")
            }
            builder.append("</div>")
        }

        builder.append("</div></body></html>")
        return builder.toString()
    }

    private fun getMoodString(context: Context, moodVal: String?): String {
        return when (moodVal) {
            "1" -> "Mal"
            "2" -> "Regular"
            "3" -> "Bien"
            "4" -> "Excelente"
            else -> moodVal ?: "-"
        }
    }
}