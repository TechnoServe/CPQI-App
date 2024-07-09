package com.technoserve.cpqi.data
import android.os.Parcel
import android.os.Parcelable

data class StatisticsData(
    val date: String?,
    val cwsName: String?,
    val score: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(date)
        parcel.writeString(cwsName)
        parcel.writeInt(score)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StatisticsData> {
        override fun createFromParcel(parcel: Parcel): StatisticsData {
            return StatisticsData(parcel)
        }

        override fun newArray(size: Int): Array<StatisticsData?> {
            return arrayOfNulls(size)
        }
    }
}