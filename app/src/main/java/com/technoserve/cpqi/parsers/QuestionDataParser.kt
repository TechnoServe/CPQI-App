package com.technoserve.cpqi.parsers

import com.technoserve.cpqi.data.Questions
import org.json.JSONObject

fun questionParser(jsonString: JSONObject, parentId: Int, catId: Int): List<Questions> {
    val items = mutableListOf<Questions>()
    val catJsonArray =
        JSONObject((jsonString).getJSONArray("audits")[parentId - 1].toString())
            .getJSONArray("categories")
    val qJsonArray = JSONObject(catJsonArray[catId - 1].toString()).getJSONArray("questions")
    for (i in 0 until qJsonArray.length()) {
        val itemJson: JSONObject = qJsonArray.getJSONObject(i)
        val id = itemJson.getLong("id")
        val qName = itemJson.getString("text")
        val categoryId = catId - 1

        val listItem = Questions(id, qName, categoryId.toLong())
        items.add(listItem)
    }

    return items
}
