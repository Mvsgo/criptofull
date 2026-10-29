package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.AnnualIrpfReport
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width (pt)
    private const val PAGE_HEIGHT = 842 // A4 standard height (pt)
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_RIGHT = 555f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT

    fun generateAndSharePdf(context: Context, report: AnnualIrpfReport) {
        val file = generatePdfFile(context, report)
        sharePdfFile(context, file, report.year)
    }

    fun generatePdfFile(context: Context, report: AnnualIrpfReport): File {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(10, 25, 47)
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(80, 95, 115)
            textSize = 10f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val headerBoxPaint = Paint().apply {
            color = Color.rgb(240, 247, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardBorderPaint = Paint().apply {
            color = Color.rgb(210, 225, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val sectionTitlePaint = Paint().apply {
            color = Color.rgb(0, 85, 150)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val boldTextPaint = Paint().apply {
            color = Color.rgb(30, 40, 50)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val regularTextPaint = Paint().apply {
            color = Color.rgb(60, 70, 80)
            textSize = 9.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val greenPaint = Paint().apply {
            color = Color.rgb(0, 150, 80)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(220, 225, 230)
            strokeWidth = 0.8f
        }

        var y = 45f

        fun checkPageBreak(neededHeight: Float) {
            if (y + neededHeight > PAGE_HEIGHT - 45f) {
                // Page footer
                regularTextPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Página $pageNumber", MARGIN_RIGHT, PAGE_HEIGHT - 25f, regularTextPaint)
                regularTextPaint.textAlign = Paint.Align.LEFT
                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 45f
            }
        }

        // Header Title Banner
        canvas.drawRoundRect(MARGIN_LEFT, y, MARGIN_RIGHT, y + 60f, 8f, 8f, headerBoxPaint)
        canvas.drawRoundRect(MARGIN_LEFT, y, MARGIN_RIGHT, y + 60f, 8f, 8f, cardBorderPaint)

        canvas.drawText("CriptoFull", MARGIN_LEFT + 14f, y + 25f, titlePaint)
        canvas.drawText("Relatório Fiscal & Demonstrativo Anual de Criptoativos", MARGIN_LEFT + 14f, y + 40f, subtitlePaint)

        val emissionDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        regularTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Ano-Calendário: ${report.year}", MARGIN_RIGHT - 14f, y + 25f, boldTextPaint)
        canvas.drawText("Emitido em: $emissionDate", MARGIN_RIGHT - 14f, y + 40f, subtitlePaint)
        regularTextPaint.textAlign = Paint.Align.LEFT

        y += 75f

        // 1. Resumo Fiscal Anual
        canvas.drawText("1. Resumo Fiscal Consolidado (Declaração de Ajuste Anual)", MARGIN_LEFT, y, sectionTitlePaint)
        y += 10f

        val summaryCardTop = y
        val summaryCardHeight = 70f
        canvas.drawRoundRect(MARGIN_LEFT, summaryCardTop, MARGIN_RIGHT, summaryCardTop + summaryCardHeight, 6f, 6f, headerBoxPaint)
        canvas.drawRoundRect(MARGIN_LEFT, summaryCardTop, MARGIN_RIGHT, summaryCardTop + summaryCardHeight, 6f, 6f, cardBorderPaint)

        val col1X = MARGIN_LEFT + 12f
        val col2X = MARGIN_LEFT + 180f
        val col3X = MARGIN_LEFT + 360f

        canvas.drawText("Rendimentos Isentos (Art. 35k):", col1X, summaryCardTop + 22f, regularTextPaint)
        canvas.drawText("R$ ${String.format("%,.2f", report.totalExemptProfitsYearBrl)}", col1X, summaryCardTop + 42f, greenPaint)

        canvas.drawText("Ganhos Líquidos Tributáveis:", col2X, summaryCardTop + 22f, regularTextPaint)
        canvas.drawText("R$ ${String.format("%,.2f", report.totalTaxableProfitsYearBrl)}", col2X, summaryCardTop + 42f, boldTextPaint)

        canvas.drawText("Total Imposto Devido (DARF):", col3X, summaryCardTop + 22f, regularTextPaint)
        val taxColor = if (report.totalTaxesDueYearBrl > 0) Color.rgb(200, 100, 0) else Color.rgb(0, 150, 80)
        val taxPaint = Paint(boldTextPaint).apply { color = taxColor }
        canvas.drawText("R$ ${String.format("%,.2f", report.totalTaxesDueYearBrl)}", col3X, summaryCardTop + 42f, taxPaint)

        y += summaryCardHeight + 25f

        // 2. Tabela Mensal de Apurações
        checkPageBreak(180f)
        canvas.drawText("2. Demonstrativo Mensal de Alienações e Apurações", MARGIN_LEFT, y, sectionTitlePaint)
        y += 12f

        // Table Header
        canvas.drawRect(MARGIN_LEFT, y, MARGIN_RIGHT, y + 20f, headerBoxPaint)
        canvas.drawRect(MARGIN_LEFT, y, MARGIN_RIGHT, y + 20f, cardBorderPaint)

        canvas.drawText("Mês", MARGIN_LEFT + 8f, y + 14f, boldTextPaint)
        canvas.drawText("Alienações (R$)", MARGIN_LEFT + 75f, y + 14f, boldTextPaint)
        canvas.drawText("Lucro Isento", MARGIN_LEFT + 190f, y + 14f, boldTextPaint)
        canvas.drawText("Lucro Trib.", MARGIN_LEFT + 300f, y + 14f, boldTextPaint)
        canvas.drawText("DARF Devido", MARGIN_LEFT + 410f, y + 14f, boldTextPaint)

        y += 20f

        val sortedMonths = report.monthlyReports.sortedBy { it.month }
        if (sortedMonths.isEmpty()) {
            canvas.drawText("Nenhuma operação registrada no ano-calendário.", MARGIN_LEFT + 8f, y + 18f, regularTextPaint)
            y += 28f
        } else {
            for (m in sortedMonths) {
                checkPageBreak(22f)
                canvas.drawLine(MARGIN_LEFT, y, MARGIN_RIGHT, y, linePaint)

                val monthLabel = String.format("%02d/%d", m.month, m.year)
                canvas.drawText(monthLabel, MARGIN_LEFT + 8f, y + 15f, regularTextPaint)
                canvas.drawText(String.format("%,.2f", m.totalSalesBrl), MARGIN_LEFT + 75f, y + 15f, regularTextPaint)
                canvas.drawText(String.format("%,.2f", m.exemptProfitBrl), MARGIN_LEFT + 190f, y + 15f, regularTextPaint)
                canvas.drawText(String.format("%,.2f", m.taxableProfitBrl), MARGIN_LEFT + 300f, y + 15f, regularTextPaint)

                val mTaxPaint = if (m.taxDueBrl > 0) taxPaint else regularTextPaint
                canvas.drawText(String.format("%,.2f", m.taxDueBrl), MARGIN_LEFT + 410f, y + 15f, mTaxPaint)

                y += 20f
            }
        }

        y += 20f

        // 3. Ficha de Bens e Direitos (Posição em 31/12)
        checkPageBreak(120f)
        canvas.drawText("3. Ficha de Bens e Direitos (Posição em 31/12/${report.year})", MARGIN_LEFT, y, sectionTitlePaint)
        y += 6f
        canvas.drawText("Valores e textos de discriminação para declaração no programa IRPF da Receita Federal", MARGIN_LEFT, y + 10f, subtitlePaint)
        y += 22f

        if (report.assetsPositionDecember31.isEmpty()) {
            checkPageBreak(30f)
            canvas.drawText("Nenhum criptoativo em estoque no final do ano-calendário.", MARGIN_LEFT, y + 10f, regularTextPaint)
            y += 25f
        } else {
            for (asset in report.assetsPositionDecember31) {
                checkPageBreak(95f)

                // Asset card container
                val assetTop = y
                val cardH = 80f
                canvas.drawRoundRect(MARGIN_LEFT, assetTop, MARGIN_RIGHT, assetTop + cardH, 5f, 5f, headerBoxPaint)
                canvas.drawRoundRect(MARGIN_LEFT, assetTop, MARGIN_RIGHT, assetTop + cardH, 5f, 5f, cardBorderPaint)

                // Asset Header
                canvas.drawText(
                    "${asset.coinName} (${asset.coinSymbol}) - Grupo ${asset.irpfCategory.group}, Código ${asset.irpfCategory.code}",
                    MARGIN_LEFT + 10f,
                    assetTop + 18f,
                    boldTextPaint
                )

                regularTextPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Custódia: ${asset.exchangeName}", MARGIN_RIGHT - 10f, assetTop + 18f, boldTextPaint)
                regularTextPaint.textAlign = Paint.Align.LEFT

                // Details Row
                canvas.drawText(
                    "Quantidade em 31/12: ${asset.quantity} ${asset.coinSymbol}   |   Custo Médio: R$ ${String.format("%,.2f", asset.averagePriceBrl)}",
                    MARGIN_LEFT + 10f,
                    assetTop + 34f,
                    regularTextPaint
                )

                regularTextPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(
                    "Situação em 31/12: R$ ${String.format("%,.2f", asset.totalAcquisitionCostBrl)}",
                    MARGIN_RIGHT - 10f,
                    assetTop + 34f,
                    greenPaint
                )
                regularTextPaint.textAlign = Paint.Align.LEFT

                canvas.drawLine(MARGIN_LEFT + 8f, assetTop + 42f, MARGIN_RIGHT - 8f, assetTop + 42f, linePaint)

                // Discrimination text wrapped
                canvas.drawText("Discriminação IRPF:", MARGIN_LEFT + 10f, assetTop + 54f, boldTextPaint)
                val discriminationText = asset.formattedDiscrimination
                val wrapped = wrapText(discriminationText, CONTENT_WIDTH - 20f, regularTextPaint)
                var dy = assetTop + 67f
                for (line in wrapped.take(2)) {
                    canvas.drawText(line, MARGIN_LEFT + 10f, dy, regularTextPaint)
                    dy += 12f
                }

                y += cardH + 10f
            }
        }

        // Final footer on last page
        regularTextPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Relatório Contábil gerado pelo CriptoFull Mobile • Conformidade com IN RFB 1.888/2019",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - 25f,
            subtitlePaint
        )
        regularTextPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Página $pageNumber", MARGIN_RIGHT, PAGE_HEIGHT - 25f, regularTextPaint)
        regularTextPaint.textAlign = Paint.Align.LEFT

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val fileName = "Relatorio_IRPF_${report.year}.pdf"
        val pdfFile = File(context.cacheDir, fileName)
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }

    private fun sharePdfFile(context: Context, file: File, year: Int) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Completo IRPF CriptoFull $year")
            putExtra(Intent.EXTRA_TEXT, "Segue em anexo o Relatório Fiscal Completo e Ficha de Bens e Direitos para o IRPF $year gerado pelo CriptoFull.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Exportar Relatório PDF ($year)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun wrapText(text: String, maxWidth: Float, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        return lines
    }
}
