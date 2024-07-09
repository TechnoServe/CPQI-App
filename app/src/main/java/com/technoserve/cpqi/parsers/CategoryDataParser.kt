package com.technoserve.cpqi.parsers

import com.technoserve.cpqi.data.Categories
import com.technoserve.cpqi.data.Questions
import org.json.JSONObject

fun categoryParser(jsonString: String, parentId: Int): List<Categories> {
    val items = mutableListOf<Categories>()
    val jsonArray =
        JSONObject(JSONObject(jsonString).getJSONArray("audits")[parentId - 1].toString())
            .getJSONArray("categories")

    for (i in 0 until jsonArray.length()) {
        val itemJson: JSONObject = jsonArray.getJSONObject(i)
        val id = itemJson.getLong("id")
        val name = itemJson.getString("name")
        val iconUrl = itemJson.getString("icon_path")
        val auditId = (parentId - 1).toLong()

        val listItem = Categories(id, name, iconUrl, auditId)
        items.add(listItem)
    }

    return items
}

fun allAuditQuestionsParser(jsonString: String, auditId: Int): List<Questions> {
    val items = mutableListOf<Questions>()
    val catJsonArray =
        JSONObject(JSONObject(jsonString).getJSONArray("audits")[auditId - 1].toString())
            .getJSONArray("categories")

    for (i in 0 until catJsonArray.length()) {
        val qJsonArray = JSONObject(catJsonArray[i].toString()).getJSONArray("questions")
        for (j in 0 until qJsonArray.length()) {
            val itemJson: JSONObject = qJsonArray.getJSONObject(j)
            val id = itemJson.getLong("id")
            val qName = itemJson.getString("text")
            val categoryId = i.toLong()

            val listItem = Questions(id, qName, categoryId)
            items.add(listItem)
        }
    }

    return items
}