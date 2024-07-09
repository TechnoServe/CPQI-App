package com.technoserve.cpqi.parsers

import com.technoserve.cpqi.data.AuditCategories
import org.json.JSONObject

fun auditParser(jsonString: String): List<AuditCategories> {
    val items = mutableListOf<AuditCategories>()
    val jsonArray = JSONObject(jsonString).getJSONArray("audits")

    for (i in 0 until jsonArray.length()) {
        val itemJson: JSONObject = jsonArray.getJSONObject(i)
        val id = itemJson.getLong("id")
        val name = itemJson.getString("name")
        val iconUrl = itemJson.getString("icon_path")
        val listItem = AuditCategories(id, name, iconUrl)

        items.add(listItem)
    }

    return items
}
