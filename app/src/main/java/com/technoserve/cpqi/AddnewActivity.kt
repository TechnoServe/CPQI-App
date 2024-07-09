package com.technoserve.cpqi

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.technoserve.cpqi.adapters.AddNewListAdapter
import com.technoserve.cpqi.adapters.CategoryAdapter
import com.technoserve.cpqi.data.Answers
import com.technoserve.cpqi.data.AppDatabase
import com.technoserve.cpqi.data.Categories
import com.technoserve.cpqi.data.Cws
import com.technoserve.cpqi.data.Questions
import com.technoserve.cpqi.data.RecordedAudit
import com.technoserve.cpqi.data.StatisticsData
import com.technoserve.cpqi.parsers.allAuditQuestionsParser
import com.technoserve.cpqi.parsers.categoryParser
import com.technoserve.cpqi.utils.isTodayDate
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlin.properties.Delegates


class AddNewActivity : AppCompatActivity(), AddNewListAdapter.OnItemClickListener,
    CategoryAdapter.OnItemClickListener {
    private var auditName = ""
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var adapter: AddNewListAdapter
    private lateinit var db: AppDatabase
    private lateinit var items: List<Questions>
    private lateinit var categories: List<Categories>
    private lateinit var getAnswers: Array<Answers>
    private var auditId by Delegates.notNull<Int>()
    private lateinit var audit: String
    private lateinit var uniqueResult: Map<String, RecordedAudit>

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addnew)
        supportActionBar?.hide()
        //check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        } else {
//            openDocumentPicker()
        }
        val backIconBtn: ImageView = findViewById(R.id.backIcon)
        val toolBarTitle: TextView = findViewById(R.id.toolbarTitle)
        auditId = intent.getIntExtra("auditId", 0)
        audit = intent.getStringExtra("audit").toString()
        items = allAuditQuestionsParser(audit, auditId)

        categories = categoryParser(audit, auditId)

        val parsedAudit =
            JSONObject(JSONObject(audit).getJSONArray("audits")[auditId - 1].toString())

//        toolBarTitle.text = if (parsedAudit.has("name")) parsedAudit["name"].toString() else "Audit"
        //set toolbar title text CPQI Checklists when in en language else use lisite yigenzura
        toolBarTitle.text = if (Locale.getDefault().language == "en") "CPQI Checklists" else "Lisite yigenzura rya CPQI"
        auditName = if (parsedAudit.has("name")) parsedAudit["name"].toString() else "Audit"
        sharedPreferences = getSharedPreferences("AnswersPref", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        setupUI(audit, auditId)

        backIconBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor")
    private fun setupUI(audit: String, auditId: Int) {
        val addNewBtn = findViewById<Button>(R.id.addNewBtn)
        val viewStatistics = findViewById<Button>(R.id.viewStatisticsBtn)

//        access answers from room db
        db = AppDatabase.getDatabase(this)!!

        getAnswers = db.answerDao().getAllByAuditId(auditId)

        addNewBtn.setOnClickListener {
            openCategoryActivity(auditId, audit)
        }
        viewStatistics.setOnClickListener {
            openStatisticsActivity(viewStatistics)
        }

        // Remove date filtering logic here
        val result = getAnswers.map {
            RecordedAudit(
                null,
                it.auditId.toInt(),
                cwsName = it.cwsName,
                respondent = it.responderName,
                score = if (it.answer == Answers.YES) 1 else 0,
                //DATE IN ENGLISH FORMAT
                date = it.date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"), Locale.ENGLISH    ),
                groupedAnswersId = it.groupedAnswersId
            )
        }
        uniqueResult = result.groupBy { it.groupedAnswersId }.mapValues { (_, audits) ->
            audits.reduce { _, audit ->
                RecordedAudit(
                    null,
                    audit.auditId,
                    audit.cwsName,
                    audit.respondent,
                    audits.sumOf { it.score },
                    audit.groupedAnswersId,
                    audit.date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"), Locale.ENGLISH)
                )
            }
        }
        emptyView = findViewById(R.id.emptyTextView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter =
            AddNewListAdapter(uniqueResult, result.size, items.size, getAnswers.toList(), this)
        recyclerView.adapter = adapter
        adapter.setupItemDecoration(recyclerView)

        if (result.isEmpty() || result[result.size - 1].auditId != auditId) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("RestrictedApi")
    fun showDropdownMenu(v: View?) {
        val menuBuilder = MenuBuilder(this)
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.dropdown_menu, menuBuilder)
        val optionsMenu = v?.let { MenuPopupHelper(this, menuBuilder, it) }
        optionsMenu?.setForceShowIcon(true)

        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.export_option -> {
                        // Export data to CSV
                        downloadCsv(this@AddNewActivity)
                        Toast.makeText(this@AddNewActivity, "Exporting data...", Toast.LENGTH_SHORT)
                            .show()
                        true
                    }

                    R.id.import_option -> {
                        // Import data from CSV
                        openDocumentPicker()
                        Toast.makeText(this@AddNewActivity, "Importing data...", Toast.LENGTH_SHORT)
                            .show()
                        true
                    }

                    else -> false
                }
            }

            override fun onMenuModeChange(menu: MenuBuilder) {}
        })

        optionsMenu?.show()
    }

    // Assuming this function is called from an Activity or Fragment
    fun downloadCsv(activity: Activity) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(
                Intent.EXTRA_TITLE, "cpqi_audits-${
                    System.currentTimeMillis()
                }.csv"
            )
        }
        activity.startActivityForResult(intent, requestCodeCreateDocument)
    }

    private val requestCodeCreateDocument = 1001

    // This function should be called from onActivityResult in the calling Activity or Fragment
    private fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        uniqueResult: Map<String, RecordedAudit>,
        activity: Activity
    ) {
        if (requestCode == requestCodeCreateDocument && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    PrintWriter(outputStream.bufferedWriter()).use { writer ->
                        writer.println("Date,Audit,Category,Question,Answer,CWS Name,Respondent,Total Answered,Score Percentage,Grouped Answers Id")
                        for (p in uniqueResult) {
                            var line: String
                            for (answer in getAnswers.toList().filter {
                                it.groupedAnswersId == p.value.groupedAnswersId
                            }) {
                                line =
                                    "${p.value.date},${JSONObject(JSONObject(audit).getJSONArray("audits")[p.value.auditId - 1].toString())["name"]},${
                                        categories.find {
                                            it.id == (items.find { i -> i.id == answer.qId }?.catId?.plus(
                                                1
                                            ))
                                        }?.name
                                    },\"${
                                        items.find { it.id == answer.qId }?.qName
                                    }\",${answer.answer},${p.value.cwsName},${p.value.respondent},${
                                        getAnswers.toList().filter {
                                            it.groupedAnswersId == p.value.groupedAnswersId
                                        }.size
                                    } /  ${items.size},${
                                        p.value.score * 100 / items.size
                                    }%,${answer.groupedAnswersId}"

                                writer.println(line)
                            }
                        }
                        writer.flush()
                    }
                }
            }
        }
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, requestCodeOpenDocument)
    }


    private val requestCodeOpenDocument = 101

    @SuppressLint("Range")
    private fun getRealPathFromURI(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            val name = cursor.getString(index)
            val path = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE))
            cursor.close()
            return path
        }
        return null
    }

    //read a cvs file
    private fun readCsvFile(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()

        if (lines.isEmpty() || !lines[0].contains("Date,Audit,Category,Question,Answer,CWS Name,Respondent,Total Answered,Score Percentage,Grouped Answers Id")) {
            Toast.makeText(this, "Invalid file format", Toast.LENGTH_SHORT).show()
            return
        }
        val data = lines.subList(1, lines.size)
        val answers = mutableListOf<Answers>()
        for (line in data) {
            val values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
            val date = values[0].trim('"')
            val auditName = values[1].trim('"')
            val questionName = values[3].trim('"')
            val answer = values[4].trim('"')
            val cwsName = values[5].trim('"')
            val respondent = values[6].trim('"')
            val groupedAnswersId = values[9].trim('"')
            val auditId = getAuditIdFromName(auditName)
            val questionId = getQuestionIdFromName(questionName, auditId)
            val newCws = Cws(
                id = UUID.randomUUID(),
                cwsName,
                "",
                "",
            )
            if (db.cwsDao().getCwsByName(cwsName) == null) db.cwsDao().insert(newCws)

//            format result to Answers object
            val answerObj = Answers(
                if (db.answerDao()
                        .getAnswerByQuestionId(questionId, groupedAnswersId) == null
                ) null else db.answerDao().getAnswerByQuestionId(
                    questionId,
                    groupedAnswersId
                )?.id,
                respondent,
                answer,
                questionId,
                auditId,
                cwsName,
                groupedAnswersId,
                date
            )
            answers.add(answerObj)
        }

        try {
            answers.forEach {
                if (db.answerDao().getAnswerByQuestionId(it.qId, it.groupedAnswersId) == null) {
                    db.answerDao().insert(it)
                } else {
                    db.answerDao().updateAnswer(arrayOf(it))
                }
            }
            Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show()
            //refresh activity
            finish()
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error importing data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAuditIdFromName(auditName: String): Long {
        val jsonObject = JSONObject(intent.getStringExtra("audit") ?: "{}")
        val audits = jsonObject.getJSONArray("audits")

        for (i in 0 until audits.length()) {
            val audit = audits.getJSONObject(i)
            if (audit.getString("name") == auditName) {
                return audit.getLong("id")
            }
        }

        return -1L
    }
    private fun getQuestionIdFromName(questionName: String, auditId: Long): Long {
        val jsonObject = JSONObject(intent.getStringExtra("audit") ?: "{}")
        val audits = jsonObject.getJSONArray("audits")

        for (i in 0 until audits.length()) {
            val audit = audits.getJSONObject(i)
            if (audit.getLong("id") == auditId) {
                val categories = audit.getJSONArray("categories")
                for (j in 0 until categories.length()) {
                    val category = categories.getJSONObject(j)
                    val questions = category.getJSONArray("questions")
                    for (k in 0 until questions.length()) {
                        val question = questions.getJSONObject(k)
                        if (question.getString("text") == questionName) {
                            return question.getLong("id")
                        }
                    }
                }
            }
        }

        return -1L
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCodeCreateDocument) {
            handleActivityResult(requestCode, resultCode, data, uniqueResult, this)
        } else if (requestCode == requestCodeOpenDocument && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    readCsvFile(uri)
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid file format", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun getDataForStatistics(): List<StatisticsData> {
        val statisticsData = mutableListOf<StatisticsData>()

        for (entry in uniqueResult.entries) {
            val recordedAudit = entry.value
            statisticsData.add(
                StatisticsData(
                    date = recordedAudit.date,
                    cwsName = recordedAudit.cwsName,
                    score = recordedAudit.score
                )
            )
        }

        return statisticsData
    }
    private fun openCategoryActivity(auditId: Int, audit: String?) {
        val intent = Intent(this, CategoriesActivity::class.java)
        intent.putExtra("auditId", auditId)
        intent.putExtra("audit", audit)
        intent.putExtra("auditName", auditName)
        startActivity(intent)
    }
    //open statistics activity
    private fun openStatisticsActivity(view: View) {
        val intent = Intent(this, Statistics::class.java)
        intent.putExtra("auditId", auditId)
        intent.putExtra("audit", audit)
        startActivity(intent)
        val statisticsData = getDataForStatistics()
        intent.putParcelableArrayListExtra("statisticsData", ArrayList(statisticsData))
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this@AddNewActivity, CategoriesActivity::class.java)
        intent.putExtra("auditId", auditId)
        intent.putExtra("audit", audit)
        intent.putExtra("auditName", auditName)
        if (db.answerDao().getAllByAuditId(auditId).isNotEmpty()) {
            intent.putExtra(
                "selectedGroupedAnswerId",
                uniqueResult.entries.elementAt(position).value.groupedAnswersId
            )
            intent.putExtra(
                "editMode", isTodayDate(
                    uniqueResult.entries.elementAt(position).value.date
                )
            )
            intent.putExtra(
                "viewMode", !isTodayDate(
                    uniqueResult.entries.elementAt(position).value.date
                )
            )
        }
        startActivity(intent)
    }
}