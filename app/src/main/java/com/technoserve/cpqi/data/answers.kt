package com.technoserve.cpqi.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "answers")

data class Answers(
    @PrimaryKey(autoGenerate = true) val id: Long? = 0,
    @ColumnInfo(name = "responder_name") val responderName: String,
    var answer: String,
    @ColumnInfo(name = "q_id") val qId: Long,
    @ColumnInfo(name = "audit_id") val auditId: Long,
    @ColumnInfo(name = "cws_name") val cwsName: String,
    @ColumnInfo(name = "grouped_answers_id") var groupedAnswersId: String,
    @ColumnInfo(name = "date") val date: String = Date().toString()
) {
    companion object {
        const val YES = "yes"
        const val NO = "no"
        const val SKIP = "skip"
    }
}