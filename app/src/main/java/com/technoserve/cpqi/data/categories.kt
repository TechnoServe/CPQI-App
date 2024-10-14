package com.technoserve.cpqi.data

data class Categories(
    val id: Long = 0,
    var name: String,
    val iconPath: String,
    val auditId: Long = 0,
    var completed: Boolean = false
)
