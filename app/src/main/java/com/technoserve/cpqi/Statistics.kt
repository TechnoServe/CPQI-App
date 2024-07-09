package com.technoserve.cpqi

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.androidplot.xy.*
import com.technoserve.cpqi.data.Answers
import com.technoserve.cpqi.data.AppDatabase
import com.technoserve.cpqi.data.Cws
import com.technoserve.cpqi.data.Questions
import com.technoserve.cpqi.data.RecordedAudit
import com.technoserve.cpqi.parsers.allAuditQuestionsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class Statistics : AppCompatActivity() {
    private lateinit var cwsName: Spinner
    private lateinit var plot: XYPlot
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var lineChart: LineChart
    private lateinit var db: AppDatabase
    private var auditId by Delegates.notNull<Int>()
    private lateinit var audit: String
    private lateinit var items: List<Questions>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        supportActionBar?.hide()
        db = AppDatabase.getDatabase(this)!!
        cwsName = findViewById(R.id.cwsNameSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)
        monthSpinner = findViewById(R.id.monthSpinner)
        auditId = intent.getIntExtra("auditId", 0)
        audit = intent.getStringExtra("audit").toString()
        items = allAuditQuestionsParser(audit, auditId)

        fetchCwsData()
        setupMonthSpinner()
        setupYearSpinner()

        val backIconBtn: ImageView = findViewById(R.id.backIcon)
        backIconBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        plot = findViewById(R.id.plot)
    }

    private fun setupMonthSpinner() {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val monthArray = resources.getStringArray(R.array.months)
        val defaultMonth = monthArray.getOrNull(currentMonth) ?: ""

        ArrayAdapter.createFromResource(
            this,
            R.array.months,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            monthSpinner.adapter = adapter
            monthSpinner.setSelection(adapter.getPosition(defaultMonth) + 1)
        }

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateGraph()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupYearSpinner() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearArray = resources.getStringArray(R.array.years)
        val defaultYear = yearArray.getOrNull(yearArray.indexOf(currentYear.toString())) ?: ""

        ArrayAdapter.createFromResource(
            this,
            R.array.years,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = adapter
            yearSpinner.setSelection(adapter.getPosition(defaultYear))
        }

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateGraph()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun fetchCwsData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cwsList = db.cwsDao().getAll()

            val adapter = ArrayAdapter(
                this@Statistics,
                android.R.layout.simple_spinner_dropdown_item,
                getCwsNames(cwsList)
            )

            withContext(Dispatchers.Main) {
                cwsName.adapter = adapter
                adapter.notifyDataSetChanged()
                cwsName.setSelection(0)
            }
        }

        cwsName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateGraph()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun getCwsNames(cwsList: Array<Cws>): List<String> {
        val names = mutableListOf<String>()
        for (cws in cwsList) {
            names.add(cws.cwsName)
        }
        return names
    }

    private fun getMonthNumber(monthName: String): Int {
        return when (monthName) {
            "January" -> 1
            "February" -> 2
            "March" -> 3
            "April" -> 4
            "May" -> 5
            "June" -> 6
            "July" -> 7
            "August" -> 8
            "September" -> 9
            "October" -> 10
            "November" -> 11
            "December" -> 12
            else -> 0 // Default case, should not happen
        }
    }

    private fun updateGraph() {
        val selectedCws = cwsName.selectedItem.toString()
        val selectedMonthName = monthSpinner.selectedItem.toString()
        val selectedMonth = getMonthNumber(selectedMonthName)
        val selectedYear = yearSpinner.selectedItem.toString()

        lifecycleScope.launch(Dispatchers.IO) {
            val data = getDataForGraph(selectedCws, selectedMonth, selectedYear)

            withContext(Dispatchers.Main) {
                plot.clear()
                val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
                val xValues = data.map { dateFormat.parse(it.date)?.time?.toDouble() ?: 0.0 }
                val yValues = data.map { it.score.toDouble() }
                val series: XYSeries = SimpleXYSeries(
                    xValues, yValues, "Score"
                )

                val format = LineAndPointFormatter(Color.GREEN, Color.BLACK, null, null)
                plot.addSeries(series, format)

                plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).format = object : Format() {
                    override fun format(
                        obj: Any?,
                        toAppendTo: StringBuffer,
                        pos: FieldPosition
                    ): StringBuffer {
                        val date = Date((obj as Number).toLong())
                        // Format the date for display (you can adjust this format as needed)
                        val displayFormat = SimpleDateFormat("MMM dd", Locale.US)
                        return toAppendTo.append(displayFormat.format(date))
                    }
                    override fun parseObject(source: String?, pos: ParsePosition): Any? {
                        return null
                    }
                }
                plot.setRangeBoundaries(0, 100, BoundaryMode.FIXED)
                plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).format = object : Format() {
                    override fun format(
                        obj: Any?,
                        toAppendTo: StringBuffer,
                        pos: FieldPosition
                    ): StringBuffer {
                        val percentage = (obj as Number).toInt()
                        return toAppendTo.append("$percentage%")
                    }
                    override fun parseObject(source: String?, pos: ParsePosition): Any? {
                        return null
                    }
                }
                plot.linesPerRangeLabel = 2
                plot.linesPerDomainLabel = if (data.size >= 3) 4 else 3

                plot.redraw()
            }
        }
    }
    private fun formatDateForDisplay(dateString: String): String {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }
        val displayFormat = SimpleDateFormat("MMM dd", Locale.US)
        return displayFormat.format(calendar.time)
    }

    private fun getDataForGraph(cwsName: String, month: Int, year: String): List<RecordedAudit> {
        val monthString = if (month < 10) "0$month" else month.toString()
        val answers = db.answerDao().getAllByCwsAndDate(cwsName, monthString, year)

        val result = answers.map {
            RecordedAudit(
                null,
                it.auditId.toInt(),
                it.cwsName,
                it.responderName,
                if (it.answer == Answers.YES) 1 else 0,
                it.groupedAnswersId,
                it.date
            )
        }
        val groupedAudits = LinkedHashMap<String, MutableList<RecordedAudit>>()

        result.forEach { audit ->
            val dateKey = formatDateKey(audit.date)
            groupedAudits.getOrPut(dateKey) { mutableListOf() }.add(audit)
        }

        return groupedAudits.map { (_, audits) ->
            audits.reduce { _, audit ->
                RecordedAudit(
                    null,
                    audit.auditId,
                    audit.cwsName,
                    audit.respondent,
                    ((audits.filter { it.score == 1 }.size.toDouble() / items.size) * 100).toInt(),
                    audit.groupedAnswersId,
                    audit.date
                )
            }
        }.sortedBy { it.date }
    }
    private fun formatDateKey(dateString: String): String {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH)}-${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
}
