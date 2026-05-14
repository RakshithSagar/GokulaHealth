package com.example.gokulahealth

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gokulahealth.data.MilkRecord
import com.example.gokulahealth.viewmodel.CattleViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MilkDiaryActivity : BaseActivity() {

    private lateinit var cattleViewModel: CattleViewModel
    private lateinit var milkChart: LineChart
    private lateinit var tvMonthlyAverage: TextView
    private lateinit var etPricePerLiter: EditText
    private lateinit var etDailyExpenses: EditText

    private lateinit var tvSummaryMorning: TextView
    private lateinit var tvSummaryEvening: TextView
    private lateinit var tvSummaryTotal: TextView
    private lateinit var tvSummaryGross: TextView
    private lateinit var tvSummaryNet: TextView

    private var currentCattleId: Int = 1
    private var milkRecordsList: List<MilkRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_milk_diary)

        // --- 1. NAVIGATION ---
        findViewById<ImageButton>(R.id.btnBackArrow).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }

        // --- 2. UI INITIALIZATION ---
        cattleViewModel = ViewModelProvider(this)[CattleViewModel::class.java]
        milkChart = findViewById(R.id.milkChart)
        tvMonthlyAverage = findViewById(R.id.tvMonthlyAverage)
        etPricePerLiter = findViewById(R.id.etPricePerLiter)
        etDailyExpenses = findViewById(R.id.etDailyExpenses)

        val etMorning = findViewById<EditText>(R.id.etMorning)
        val etEvening = findViewById<EditText>(R.id.etEvening)
        val btnSaveMilk = findViewById<Button>(R.id.btnSaveMilk)
        val btnDownloadReport = findViewById<Button>(R.id.btnDownloadReport)

        tvSummaryMorning = findViewById(R.id.tvSummaryMorning)
        tvSummaryEvening = findViewById(R.id.tvSummaryEvening)
        tvSummaryTotal = findViewById(R.id.tvSummaryTotal)
        tvSummaryGross = findViewById(R.id.tvSummaryGross)
        tvSummaryNet = findViewById(R.id.tvSummaryNet)

        setupLiveSummary(etMorning, etEvening, etPricePerLiter, etDailyExpenses)

        lifecycleScope.launch {
            cattleViewModel.getMilkRecords(currentCattleId).collect { records ->
                if (records.isNotEmpty()) {
                    milkRecordsList = records.sortedBy { it.date }
                    setupChartAndCalculations(milkRecordsList)
                }
            }
        }

        // --- 3. PDF DOWNLOAD ACTION ---
        btnDownloadReport.setOnClickListener {
            if (milkRecordsList.isNotEmpty()) {
                createPdfReport(milkRecordsList)
            } else {
                Toast.makeText(this, "No production data found to create a report", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 4. DATABASE SAVE LOGIC ---
        btnSaveMilk.setOnClickListener {
            val mStr = etMorning.text.toString()
            val eStr = etEvening.text.toString()
            val pStr = etPricePerLiter.text.toString()
            val expStr = etDailyExpenses.text.toString()

            if (mStr.isBlank() && eStr.isBlank()) {
                Toast.makeText(this, "Please enter yield data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val morning = mStr.toDoubleOrNull() ?: 0.0
            val evening = eStr.toDoubleOrNull() ?: 0.0
            val price = pStr.toDoubleOrNull() ?: 0.0
            val dailyExp = expStr.toDoubleOrNull() ?: 0.0

            val totalYield = morning + evening
            val gross = totalYield * price
            val net = gross - dailyExp
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val record = MilkRecord(
                cattleId = currentCattleId,
                date = currentDate,
                morningYield = morning,
                eveningYield = evening,
                grossProfit = gross,
                expenses = dailyExp,
                netProfit = net
            )

            lifecycleScope.launch {
                try {
                    cattleViewModel.insertMilkRecord(record)
                    Toast.makeText(this@MilkDiaryActivity, "Daily Log Saved!", Toast.LENGTH_SHORT).show()
                    etMorning.text.clear()
                    etEvening.text.clear()
                } catch (e: Exception) {
                    AlertDialog.Builder(this@MilkDiaryActivity).setTitle("Database Error").setMessage("${e.localizedMessage}").show()
                }
            }
        }
    }

    // --- 5. UPDATED PDF GENERATION (With Version Compatibility Check) ---
    private fun createPdfReport(records: List<MilkRecord>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true
        titlePaint.color = Color.parseColor("#1A5D1A")
        canvas.drawText("GokulaHealth - Production Report", 50f, 60f, titlePaint)

        paint.textSize = 12f
        canvas.drawText("Generated on: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}", 50f, 90f, paint)
        canvas.drawLine(50f, 110f, 545f, 110f, paint)

        var yPos = 150f
        paint.isFakeBoldText = true
        canvas.drawText("Date", 50f, yPos, paint)
        canvas.drawText("Yield (L)", 160f, yPos, paint)
        canvas.drawText("Gross (₹)", 270f, yPos, paint)
        canvas.drawText("Net Profit (₹)", 400f, yPos, paint)

        paint.isFakeBoldText = false
        yPos += 30f

        for (record in records) {
            if (yPos > 800) break
            canvas.drawText(record.date, 50f, yPos, paint)
            canvas.drawText("${record.morningYield + record.eveningYield}", 160f, yPos, paint)
            canvas.drawText(String.format("%.2f", record.grossProfit), 270f, yPos, paint)
            canvas.drawText(String.format("%.2f", record.netProfit), 400f, yPos, paint)
            yPos += 25f
        }

        pdfDocument.finishPage(page)

        val fileName = "GokulaHealth_Report_${System.currentTimeMillis()}.pdf"

        // Handle different Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Modern Scoped Storage Logic (API 29+)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            try {
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    Toast.makeText(this, "Saved to Downloads", Toast.LENGTH_LONG).show()
                    openPdf(uri)
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to generate PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument.close()
            }
        } else {
            // Legacy File Logic (API 26-28)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            try {
                pdfDocument.writeTo(FileOutputStream(file))
                // Notify the system that a new file was created so it appears in "Recent"
                MediaScannerConnection.scanFile(this, arrayOf(file.absolutePath), arrayOf("application/pdf"), null)

                Toast.makeText(this, "Saved to Downloads", Toast.LENGTH_LONG).show()

                // For legacy versions, we need to use a FileProvider to get a safe Uri
                val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
                openPdf(uri)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pdfDocument.close()
            }
        }
    }

    // --- 6. CONVENIENCE FIX: OPEN IMMEDIATELY ---
    private fun openPdf(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, "Open Report with..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }

    // --- 7. UTILITY FUNCTIONS ---
    private fun setupLiveSummary(morning: EditText, evening: EditText, rate: EditText, expenses: EditText) {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val mVal = morning.text.toString().toDoubleOrNull() ?: 0.0
                val eVal = evening.text.toString().toDoubleOrNull() ?: 0.0
                val rVal = rate.text.toString().toDoubleOrNull() ?: 0.0
                val exVal = expenses.text.toString().toDoubleOrNull() ?: 0.0
                val total = mVal + eVal
                val gross = total * rVal
                val net = gross - exVal
                tvSummaryMorning.text = String.format("%.2f L", mVal)
                tvSummaryEvening.text = String.format("%.2f L", eVal)
                tvSummaryTotal.text = String.format("%.2f L", total)
                tvSummaryGross.text = String.format("₹%.2f", gross)
                tvSummaryNet.text = String.format("₹%.2f", net)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        morning.addTextChangedListener(watcher)
        evening.addTextChangedListener(watcher)
        rate.addTextChangedListener(watcher)
        expenses.addTextChangedListener(watcher)
    }

    private fun setupChartAndCalculations(records: List<MilkRecord>) {
        val last30Days = records.takeLast(30)
        var totalSum = 0.0
        val morningEntries = ArrayList<Entry>()
        val eveningEntries = ArrayList<Entry>()

        last30Days.forEachIndexed { index, record ->
            totalSum += (record.morningYield + record.eveningYield)
            morningEntries.add(Entry(index.toFloat(), record.morningYield.toFloat()))
            eveningEntries.add(Entry(index.toFloat(), record.eveningYield.toFloat()))
        }

        if (last30Days.isNotEmpty()) {
            val avg = totalSum / last30Days.size
            tvMonthlyAverage.text = getString(R.string.monthly_avg, avg)
        }

        milkChart.setTouchEnabled(true)
        milkChart.isHighlightPerTapEnabled = true

        milkChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val index = e?.x?.toInt() ?: -1
                if (index in last30Days.indices) {
                    val record = last30Days[index]
                    val dailyTotal = record.morningYield + record.eveningYield
                    val message = """
                        📅 Date: ${record.date}
                        🌅 Morning: ${record.morningYield} L
                        🌇 Evening: ${record.eveningYield} L
                        📊 Total Yield: $dailyTotal L
                        -------------------------
                        💰 Gross Profit: ₹${String.format("%.2f", record.grossProfit)}
                        💸 Expenses: ₹${String.format("%.2f", record.expenses)}
                        ✅ NET PROFIT: ₹${String.format("%.2f", record.netProfit)}
                    """.trimIndent()
                    AlertDialog.Builder(this@MilkDiaryActivity).setTitle("Historical Record").setMessage(message).setPositiveButton("OK", null).show()
                }
            }
            override fun onNothingSelected() {}
        })

        val mSet = LineDataSet(morningEntries, "AM").apply {
            color = Color.parseColor("#FFA000")
            lineWidth = 2.5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        val eSet = LineDataSet(eveningEntries, "PM").apply {
            color = Color.parseColor("#1E88E5")
            lineWidth = 2.5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        milkChart.data = LineData(mSet, eSet)
        milkChart.description.isEnabled = false
        milkChart.animateX(800)
        milkChart.invalidate()
    }
}