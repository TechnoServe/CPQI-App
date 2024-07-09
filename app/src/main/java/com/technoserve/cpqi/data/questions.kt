package com.technoserve.cpqi.data

data class Questions(
    val id: Long = 0,
    val qName: String,
    val catId: Long,
    val weight: Int = 1
)
