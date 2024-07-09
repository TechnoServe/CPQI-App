package com.technoserve.cpqi.utils

import org.json.JSONObject

fun getAuditId(jsonString: String, questionId: Int): Int? {
    // Parse JSON string
    val jsonObject = JSONObject(jsonString)

    // Get the "audits" array
    val auditsArray = jsonObject.getJSONArray("audits")

    // Iterate through audits
    for (i in 0 until auditsArray.length()) {
        val auditObject = auditsArray.getJSONObject(i)

        // Get categories array
        val categoriesArray = auditObject.getJSONArray("categories")

        // Iterate through categories
        for (j in 0 until categoriesArray.length()) {
            val categoryObject = categoriesArray.getJSONObject(j)

            // Get questions array
            val questionsArray = categoryObject.getJSONArray("questions")

            // Iterate through questions
            for (k in 0 until questionsArray.length()) {
                val questionObject = questionsArray.getJSONObject(k)

                // Check if question id matches the specified question id
                if (questionObject.getInt("id") == questionId) {
                    // Return the audit id associated with this question
                    return auditObject.getInt("id")
                }
            }
        }
    }
    // Return null if question id not found
    return null
}