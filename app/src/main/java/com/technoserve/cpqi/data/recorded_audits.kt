package com.technoserve.cpqi.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.Locale


@Entity(tableName = "recordedAudit")
data class RecordedAudit(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    @ColumnInfo(name = "auditId") val auditId: Int,
    @ColumnInfo(name = "cwsName") val cwsName: String,
    @ColumnInfo(name = "respondent") val respondent: String,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "groupedAnswersId") val groupedAnswersId: String,
    @ColumnInfo(name = "date") val date: String = Date().toString(),
)
