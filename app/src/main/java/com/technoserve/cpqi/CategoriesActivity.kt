package com.technoserve.cpqi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import com.technoserve.cpqi.utils.getCurrentDateFormatted
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.technoserve.cpqi.adapters.CategoryAdapter
import com.technoserve.cpqi.data.Answers
import com.technoserve.cpqi.data.AppDatabase
import com.technoserve.cpqi.data.Categories
import com.technoserve.cpqi.data.Cws
import com.technoserve.cpqi.data.Questions
import com.technoserve.cpqi.parsers.allAuditQuestionsParser
import com.technoserve.cpqi.parsers.categoryParser
import com.technoserve.cpqi.utils.formatDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.properties.Delegates

class CategoriesActivity : AppCompatActivity(), CategoryAdapter.OnItemClickListener,
    PopupActivity.DialogDismissListener, View.OnClickListener {
    companion object {
        private const val REQUEST_CODE_ADD_CWS = 100
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var audit: String
    private var chart: Int = 0
    private var auditId by Delegates.notNull<Int>()
    private lateinit var respondentContainer: LinearLayout
    private lateinit var respondent: TextView
    private lateinit var submitAll: Button
    private lateinit var dialog: PopupActivity
    private var answerDetails: Array<Answers> = emptyArray()
    private lateinit var cwsName: Spinner
    private var progress = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var percentageText: TextView
    private lateinit var addStation: Button
    private lateinit var cwsEditText: TextView
    private lateinit var cwsInputLayout: LinearLayout
    private var editMode = false
    private var viewMode = false
    private var selectedGroupedAnswerId = ""
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val gson = Gson()
    private var json: String = ""

    //    initialize room db
    private lateinit var db: AppDatabase
    private var items: List<Categories> = emptyList()
    private lateinit var respondentName: String
    private var allCatQuestions: List<Questions> = emptyList()

    @Synchronized
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)
        supportActionBar?.hide()
        db = AppDatabase.getDatabase(this)!!
        fetchCwsData()
        editMode = intent.getBooleanExtra("editMode", false)
        viewMode = intent.getBooleanExtra("viewMode", false)
        selectedGroupedAnswerId = intent.getStringExtra("selectedGroupedAnswerId").toString()
        val backIconBtn: ImageView = findViewById(R.id.backIcon)
        submitAll = findViewById(R.id.submitAllBtn)
        cwsEditText = findViewById(R.id.cwsEditText)
        val toolBarTitle: TextView = findViewById(R.id.toolbarTitle)
        auditId = intent.getIntExtra("auditId", 1)
        audit = intent.getStringExtra("audit").toString()
        items = categoryParser(audit, auditId)
        allCatQuestions = allAuditQuestionsParser(audit, auditId)
        progressBar = findViewById(R.id.scoreProgressBar)
        percentageText = findViewById(R.id.percentageText)
        addStation = findViewById(R.id.addStation)
        cwsInputLayout = findViewById(R.id.cwsInputLayout)
        onClickListener(addStation)
//        lifecycleScope.launch(Dispatchers.IO) {
//            insertInitialStationsFromJson()
//        }
        chart = R.drawable.cherry_scale
        val score = 0

        progressBar.progress = score

        // Update percentage text
        percentageText.text = "$score%"

        sharedPreferences = getSharedPreferences("AnswersPref", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        setupUI(items)

        val dateTextView = findViewById<TextView>(R.id.todaysDateTextView)
        //ensure date is all the same in en and kinyarwanda use formatDate function to format the date
        val currentDate = getCurrentDateFormatted()
        dateTextView.text = currentDate

        toolBarTitle.text = intent.getStringExtra("auditName")
        backIconBtn.setOnClickListener {
            // Go back to the previous activity

            finish()
        }

        if (viewMode) submitAll.visibility = View.GONE else submitAll.visibility = View.VISIBLE


        submitAll.isEnabled = false
        submitAll.backgroundTintList =
            ColorStateList.valueOf(resources.getColor(if (submitAll.isEnabled) R.color.maroon else R.color.maroonDisabled))

        //handle submission on new answers and already existing answers in edit mode updating the existing answers in the db

        submitAll.setOnClickListener {

            val groupedAnswersId = UUID.randomUUID().toString()

            val answers = gson.fromJson(
                sharedPreferences.getString("answers", json),
                Array<Answers>::class.java
            )
            if (editMode) {
//                loop through answers and check if id is null, add new item in answerDetails, otherwise update existing item
                answers.forEach {
                    if (it.id == null) {
                        val cwsNameValue = answerDetails.last().cwsName
                        answerDetails = answerDetails.plus(
                            Answers(
                                null,
                                it.responderName,
                                it.answer,
                                it.qId,
                                it.auditId,
                                cwsNameValue,
                                groupedAnswersId = it.groupedAnswersId,
                            )
                        )
                    } else {
                        val index = answerDetails.indexOfFirst { answer -> answer.qId == it.qId }
                        answerDetails = answerDetails.apply {
                            this[index] = it
                        }
                    }
                }
            }

            // Update each answer with a unique id
            if (!editMode) {
                answers.forEach {
                    it.groupedAnswersId = groupedAnswersId
                }
            }

//             If respondent is not selected, show error message
            if (respondent.text.isEmpty()) {
                respondent.error = getString(R.string.missing_respondent_error)
                respondent.requestFocus()
                return@setOnClickListener
            }



            if (!editMode) {
                //if the respondent is empty raise an error and after entering name update the answerDetails
                val hintText = getString(R.string.select_cws_name)
                if (cwsName.selectedItem == null || cwsName.selectedItem == hintText) {
                    Toast.makeText(
                        this,
                        applicationContext.getText(R.string.missing_cws_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    cwsName.requestFocus()
                    return@setOnClickListener
                } else {
//                update answers variable with the selected cws name
                    answers.forEach {
                        it.cwsName = cwsName.selectedItem.toString()
                    }
                }
                // Insert new answers into the database
                Thread {
                    db.answerDao().insertAll(answers)
                }.start()

            } else {
                Thread {
                    answerDetails = answerDetails.map {
                        Answers(
                            db.answerDao().getAll()
                                .find { answer -> answer.qId == it.qId && answer.groupedAnswersId == it.groupedAnswersId }?.id,
                            it.responderName,
                            it.answer,
                            it.qId,
                            it.auditId,
                            cwsName = it.cwsName,
                            groupedAnswersId = it.groupedAnswersId,
                        )
                    }.toTypedArray()

                    db.answerDao().updateAnswer(answerDetails)

                    val newAnswers = answers.filter {
//                        check if qId exists under groupedAnswersId
                        db.answerDao().getAll()
                            .none { answer -> answer.qId == it.qId && answer.groupedAnswersId == it.groupedAnswersId }
                    }.map {
                        Answers(
                            null,
                            responderName = answerDetails.last().responderName,
                            it.answer,
                            it.qId,
                            it.auditId,
                            cwsName = answerDetails.last().cwsName,
                            groupedAnswersId = answerDetails.last().groupedAnswersId,
                        )
                    }

                    db.answerDao().insertAll(newAnswers.toTypedArray())
                }.start()
            }
//             Remove shared preferences after submitting answers
            editor.remove("answers")
            editor.apply()
            submitAll.isEnabled = false

            Toast.makeText(
                this,
                applicationContext.getText(R.string.success_alert_msg),
                Toast.LENGTH_SHORT
            ).show()
            Thread.sleep(2000)

            val intent = Intent(this, AddNewActivity::class.java)
            intent.putExtra("auditId", auditId)
            intent.putExtra("audit", audit)
            startActivity(intent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")


    private fun onClickListener(addStation: Button) {
        addStation.setOnClickListener {
            openStationActivity(getSelectedLanguage())
        }
    }

    private fun openStationActivity(language: String) {
        val intent = Intent(this, NewstationActivity::class.java)
//        intent.putExtra("auditId", auditId)
        intent.putExtra("language", language)
        startActivityForResult(intent, REQUEST_CODE_ADD_CWS)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_CWS && resultCode == Activity.RESULT_OK) {
            fetchCwsData()
        }
    }

    private fun getSelectedLanguage(): String {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("language", "en") ?: "en"
    }

    private fun fetchCwsData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cwsList = db.cwsDao().getAll()
            val cwsNames = getCwsNames(cwsList).toMutableList()
            val hintText = getString(R.string.select_cws_name)
            cwsNames.add(0, hintText)
            // Create an ArrayAdapter with CWS names (or relevant data)
            val adapter = ArrayAdapter(
                this@CategoriesActivity,
                android.R.layout.simple_spinner_dropdown_item,
                cwsNames
            )

            // Update UI on the main thread
            withContext(Dispatchers.Main) {
                cwsName.adapter = adapter
                adapter.notifyDataSetChanged()
                cwsName.setSelection(0)
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

//    private fun insertInitialStationsFromJson() {
//        lifecycleScope.launch {
//            val jsonString = assets.open("stations.json").bufferedReader().use { it.readText() }
//            val gson = Gson()
//            val stationsType = object : TypeToken<List<Cws>>() {}.type
//            val stations: List<Cws> = gson.fromJson(jsonString, stationsType)
//
//            val newStations = mutableListOf<Cws>()
//            for (station in stations) {
//                val existingCws = db.cwsDao().getAllCwsByName(station.cwsName)
//                if (existingCws == null) {
//                    station.cwsLeader = ""
//                    station.location = ""
//                    newStations.add(station.copy(id = UUID.randomUUID()))
//                }
//            }
//
//            if (newStations.isNotEmpty()) {
//                db.cwsDao().insertAll(newStations)
//            }
//            withContext(Dispatchers.Main) {
//                fetchCwsData()
//            }
//        }
//    }

    private fun disableRecyclerView(recyclerView: RecyclerView) {
        // Disable all child views of the RecyclerView
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            child.isClickable = false
            child.isFocusable = false
        }
        // Change background color of RecyclerView to grey
        recyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.lightGrey))
    }

    private fun enableRecyclerView(recyclerView: RecyclerView) {
        // Enable all child views of the RecyclerView
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            child.isClickable = true
            child.isFocusable = true
        }
        // Change background color of RecyclerView to white
        recyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.LightPink1))
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(items: List<Categories>?) {
        respondentContainer = findViewById(R.id.textInputLayoutContainer)
        respondent = findViewById(R.id.nameEditText)
        cwsName = findViewById(R.id.cwsNameSpinner)


//        if editMode is true, load answers
        if (editMode) {
//            get today's answers corresponding with auditId
            answerDetails = db.answerDao().getAll()
                .filter { it.groupedAnswersId == selectedGroupedAnswerId }.toTypedArray()

            respondent.text = answerDetails.last().responderName
            respondent.isEnabled = false

            val selectedCwsName = answerDetails.firstOrNull()?.cwsName ?: ""
            cwsEditText.text = selectedCwsName
            cwsEditText.isEnabled = false

            addStation.visibility = View.GONE
            cwsName.visibility = View.GONE
            cwsInputLayout.visibility = View.VISIBLE


//            update progress bar
            progress =
                (answerDetails.count { it.answer == Answers.YES } * 100) / allCatQuestions.size
            val score = progress

            progressBar.progress = score
            percentageText.text = "$score%"
        } else if (viewMode) {
            // Get today's answers corresponding with auditId
            answerDetails = db.answerDao().getAll()
                .filter { it.groupedAnswersId == selectedGroupedAnswerId }.toTypedArray()

            respondent.text = answerDetails.last().responderName
            respondent.isEnabled = false

            val cwsList = db.cwsDao().getAll()

            // Create an ArrayAdapter with CWS names (or relevant data)
            val adapter = ArrayAdapter(
                this@CategoriesActivity,
                android.R.layout.simple_spinner_dropdown_item,
                getCwsNames(cwsList)
            )

            val selectedCwsName = answerDetails.firstOrNull()?.cwsName ?: ""
            cwsEditText.text = selectedCwsName
            cwsEditText.isEnabled = false

            addStation.visibility = View.GONE
            cwsName.visibility = View.GONE
            cwsInputLayout.visibility = View.VISIBLE

            // Update progress bar
            progress =
                (answerDetails.count { it.answer == Answers.YES } * 100) / allCatQuestions.size
            val score = progress

            progressBar.progress = score

            // Update percentage text
            percentageText.text = "$score%"
        } else {
            progressBar.progress = 0
            percentageText.text = "0%"
        }
//        add respondent name and cws name to answerDetails when the user enters them
        respondent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (answerDetails.isNotEmpty()) {
                    answerDetails = answerDetails.map {
                        Answers(
                            null,
                            s.toString(),
                            it.answer,
                            it.qId,
                            auditId.toLong(),
                            cwsName.selectedItem?.toString() ?: "",
                            ""
                        )
                    }.toTypedArray()
                } else {
                    answerDetails = answerDetails.plus(
                        Answers(
                            null,
                            s.toString(),
                            "",
                            items!![0].id,
                            auditId.toLong(),
                            cwsName.selectedItem?.toString() ?: "",
                            ""
                        )
                    )
                }

//        add shared preferences to save answers
                json = gson.toJson(answerDetails)
                editor.putString("answers", json)
                editor.apply()
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // Do nothing before text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing when text is changed
            }
        })

//        cwsName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                val selectedCwsName = cwsName.selectedItem.toString().trim().lowercase(Locale.getDefault())
//                Log.d("selectedCwsName", "selectedCwsName: $selectedCwsName")
//                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//                Log.d("today", "today: $today")
//
//
////                answerDetails = emptyArray()
//                answerDetails = db.answerDao().getAll()
//
//                 val filteredDetails= answerDetails
//                    .filter { it.cwsName.trim().lowercase(Locale.getDefault()) == selectedCwsName && formatDate(it.date) == today }
//
//                Log.d("FilteredDetails", "FilteredDetails: ${filteredDetails.joinToString()}")
//                answerDetails = filteredDetails.toTypedArray()
//
//                if (answerDetails.isNotEmpty()) {
//                    // Display error message immediately
//                    if (!editMode) {
//                        (parent?.getChildAt(0) as? TextView)?.error =
//                            getString(R.string.already_recorded_error_alert_msg)
//
//                    // display alert-dialog with error message
//                    Toast.makeText(
//                        this@CategoriesActivity,
//                        applicationContext.getText(R.string.already_recorded_error_alert_msg),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                    //disable the gridLayout below spinner
//                    if (!editMode) disableRecyclerView(recyclerView)
//                } else {
//                    // No existing answers found, proceed with adding/updating answerDetails
//                    answerDetails = arrayOf(
//                        Answers(
//                            null,
//                            respondent.text.toString(),
//                            "",
//                            items?.getOrNull(0)?.id ?: 0,
//                            auditId = auditId.toLong(),
//                            cwsName = selectedCwsName,
//                            ""
//                        )
//                    ).toList().toTypedArray()
//                    if (!editMode) enableRecyclerView(recyclerView)
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Do nothing if nothing is selected
//            }
//        }

        cwsName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Get the selected CWS name (trim and convert to lowercase for consistency)
                val selectedCwsName = cwsName.selectedItem.toString().trim().lowercase(Locale.getDefault())
                Log.d("selectedCwsName", "selectedCwsName: $selectedCwsName")

                // Get today's date in the format "yyyy-MM-dd"
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                Log.d("today", "today: $today")

                // Fetch answers from SharedPreferences
                val sharedPreferences = getSharedPreferences("AnswersPrefs", Context.MODE_PRIVATE)
                val jsonAnswers = sharedPreferences.getString("answers", "[]")
                val gson = Gson()

                // Convert the JSON string to an array of Answers
                val type = object : TypeToken<List<Answers>>() {}.type
                var answerDetails: MutableList<Answers> = gson.fromJson(jsonAnswers, type)

                // Check if the CWS name already has recorded answers for today
                val existingRecordsForToday = db.answerDao().getAll().filter {
                    it.cwsName.trim().lowercase(Locale.getDefault()) == selectedCwsName && formatDate(it.date) == today
                }

                if (existingRecordsForToday.isNotEmpty()) {
                    // If a record already exists for the selected CWS on today's date, show error message
                    if (!editMode) {
                        (parent?.getChildAt(0) as? TextView)?.error =
                            getString(R.string.already_recorded_error_alert_msg)

                        submitAll.isEnabled = false
                        submitAll.backgroundTintList =
                            ColorStateList.valueOf(resources.getColor(if (submitAll.isEnabled) R.color.maroon else R.color.maroonDisabled))
                        // Show a toast with the error message
                        Toast.makeText(
                            this@CategoriesActivity,
                            applicationContext.getText(R.string.already_recorded_error_alert_msg),
                            Toast.LENGTH_SHORT
                        ).show()

                        // Disable RecyclerView or GridLayout below spinner if not in edit mode
                        if (!editMode) disableRecyclerView(recyclerView)
                    }
                } else {
                    // If no existing answers for today, update the CWS name in the saved answers

                    if (answerDetails.isNotEmpty()) {
                        // Update only the CWS name for all answers in SharedPreferences
                        answerDetails.forEach { answer ->
                            answer.cwsName = selectedCwsName  // Only update the CWS name
                        }

                        Log.d("Updated answers", "Answers with new CWS name: $answerDetails")

                        // Save updated answers back to SharedPreferences
                        val editor = sharedPreferences.edit()
                        val updatedJsonAnswers = gson.toJson(answerDetails)
                        editor.putString("answers", updatedJsonAnswers)
                        editor.apply()

                        // Enable RecyclerView if not in edit mode
                        if (!editMode) enableRecyclerView(recyclerView)
                    } else {
                        // No previous answers exist, create a new answer
                        answerDetails = arrayOf(
                            Answers(
                                null,
                                respondent.text.toString(),
                                "",
                                items?.getOrNull(0)?.id ?: 0,
                                auditId = auditId.toLong(),
                                cwsName = selectedCwsName,
                                ""
                            )
                        ).toList().toTypedArray().toMutableList()

                        Log.d("New Answers", "New answerDetails created: $answerDetails")

                        // Enable RecyclerView if not in edit mode
                        if (!editMode) enableRecyclerView(recyclerView)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
        }
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = items?.let {
            CategoryAdapter(
                it,
                this,
                applicationContext,
                editMode,
                viewMode,
                answerDetails,
                allCatQuestions
            )
        }!!
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    override fun onItemClick(position: Int) {
        startActivityAfterClick(position)
    }

    private fun startActivityAfterClick(position: Int) {
        val auditId = intent.getIntExtra("auditId", 0)
        val hintText =
            getString(R.string.select_cws_name) // Example hint text like "Select CWS Name"

// Conditional logic for handling the selection
        val selectedCwsName = when {
//            cwsName.selectedItem == null || cwsName.selectedItem.toString() == hintText -> ""
            editMode -> cwsEditText.text.toString() // If in editMode, use the cwsEditText's text
            else -> cwsName.selectedItem.toString() // Use the selected item from spinner
        }

        if ((adapter.items[position - 1].name == "Cherry reception") && chart != 0) {
            dialog = PopupActivity(
                this,
                auditId,
                chart,
                audit,
                position,
                adapter.items[position - 1].name,
                answerDetails,
                respondent.text.toString(),
                selectedCwsName,
//                if (cwsName.selectedItem != null) if (editMode) cwsEditText.text.toString() else cwsName.selectedItem.toString() else "",
                editMode,
                viewMode,

                )
            dialog.setDismissListener(this)
            dialog.show()
        } else {
            dialog = PopupActivity(
                this,
                auditId,
                null,
                audit,
                position,
                adapter.items[position - 1].name,
                answerDetails,
                respondent.text.toString(),
                selectedCwsName,
//                if (cwsName.selectedItem != null) if (editMode) cwsEditText.text.toString() else cwsName.selectedItem.toString() else "",
                editMode,
                viewMode,

                )
            dialog.setDismissListener(this)
            dialog.show()
        }
    }
    private var isSubmitButtonClicked = false
    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onDialogDismissed(updatedAnswers: Array<Answers>?, categoryId: Int) {
        if (isSubmitButtonClicked) {
            return // Exit if the submit button is already clicked
        }
        isSubmitButtonClicked = true

        submitAll.isEnabled = false
        adapter.updateColor(categoryId)
        adapter.notifyDataSetChanged()

        updatedAnswers?.forEach { updatedAnswer ->
            val existingAnswer = answerDetails.find { it.qId == updatedAnswer.qId }
            if (existingAnswer != null) {
                // Update existing answer in answerDetails
                val index = answerDetails.indexOf(existingAnswer)
                answerDetails[index] = updatedAnswer
            } else {
                // Add new answer to answerDetails
                answerDetails = answerDetails.plus(updatedAnswer)
            }
        }

        @Synchronized
        if (answerDetails.isNotEmpty()) {

            submitAll.isEnabled = true
            submitAll.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(if (submitAll.isEnabled) R.color.maroon else R.color.maroonDisabled))
        } else {
            submitAll.isEnabled = false
            submitAll.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(if (submitAll.isEnabled) R.color.maroon else R.color.maroonDisabled))
        }

        progress = (answerDetails.count { it.answer == Answers.YES } * 100) / allCatQuestions.size
        val score = progress
        progressBar.progress = score
        // Update percentage text
        percentageText.text = "$score%"

//        add shared preferences to save answers
        json = gson.toJson(answerDetails)
        editor.putString("answers", json)
        editor.apply()


        isSubmitButtonClicked = false // Reset the flag after the operation
        submitAll.isEnabled =true

    }

    override fun onClick(v: View?) {

        TODO("Not yet implemented")
    }
}