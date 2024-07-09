package com.technoserve.cpqi


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.graphics.DashPathEffect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.androidplot.util.PixelUtils
import com.androidplot.xy.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.technoserve.cpqi.data.AppDatabase
import com.technoserve.cpqi.data.Cws
import com.technoserve.cpqi.parsers.readJsonFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var jsonData: String
    private lateinit var db: AppDatabase
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(this)!!
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)
        setupLanguageSpinner(languageSpinner)
        val initialLanguage = getInitialLanguage()
        setupUI(initialLanguage)
        onClickListener()
        val welcomeText: TextView = findViewById(R.id.welcomeText)
        val welcomeMessage = getString(R.string.welcome_message)
        startTypewriterAnimation(welcomeText, welcomeMessage)
    }
    private fun startTypewriterAnimation(textView: TextView, text: String) {
        val length = text.length
        val animator = ValueAnimator.ofInt(0, length)
        animator.duration = (length * 100).toLong() // Adjust speed by changing the multiplier
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val position = animation.animatedValue as Int
            textView.text = text.substring(0, position)
        }
        animator.start()
    }


    private fun getInitialLanguage(): String {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedLanguage = sharedPref.getString("language", null)

        return savedLanguage ?: getDefaultLanguage()
    }

    private fun getDefaultLanguage(): String {
        // Determine the default language based on your requirements
        // For example, you can use the device's default language
        return "en" // Change this to the desired default language
    }

    private fun setupLanguageSpinner(languageSpinner: Spinner) {
        ArrayAdapter.createFromResource(
            this,
            R.array.languages,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            languageSpinner.adapter = adapter
        }

        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selectedLanguage = when (position) {
                    0 -> "en"
                    1 -> "rw"
                    else -> getDefaultLanguage()
                }
                changeLanguage(selectedLanguage)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
        }
    }

    private fun getDefaultJsonFileName(language: String): String {
        return when (language) {
            "en" -> "data_en.json"
            "rw" -> "data_rw.json"
            else -> "data_en.json" // Use English as the default
        }
    }

    private fun changeLanguage(languageCode: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        saveLanguagePreference(languageCode)
        setupUI(languageCode)
    }

    private fun saveLanguagePreference(language: String) {
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("language", language)
            apply()
        }
    }

    private fun setupUI(selectedLanguage: String) {
        val languageSpinner: Spinner = findViewById(R.id.languageSpinner)
        val selectedLang = when (selectedLanguage) {
            "en" -> 0
            "rw" -> 1
            else -> 0 // Set English as the default
        }
        languageSpinner.setSelection(selectedLang)

        jsonData = readJsonFromAssets(this, getDefaultJsonFileName(selectedLanguage))
    }

    private fun onClickListener() {
        val getStarted = findViewById<Button>(R.id.get_started)
        getStarted.setOnClickListener {
            val language = getInitialLanguage()
            openAuditActivity(language)
        }
    }

    private fun openAuditActivity(language: String) {
        val intent = Intent(this, AddNewActivity::class.java)
        intent.putExtra("language", language)
        intent.putExtra("auditId", 1)
        intent.putExtra("audit", jsonData)
        startActivity(intent)
    }



}