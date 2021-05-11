package com.css.step.data

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class TodayStepData: Serializable, Parcelable {


    //当天时间，只显示到天 yyyy-MM-dd
    private var today: String? = null

    //步数时间，显示到毫秒
    private var date: Long = 0

    //对应date时间的步数
    private var step: Long = 0

    protected fun TodayStepData(`in`: Parcel) {
        today = `in`.readString()
        date = `in`.readLong()
        step = `in`.readLong()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(today)
        dest.writeLong(date)
        dest.writeLong(step)
    }

    override fun describeContents(): Int {
        return 0
    }

    @JvmField val CREATOR: Parcelable.Creator<TodayStepData?> = object : Parcelable.Creator<TodayStepData?> {
        override fun createFromParcel(`in`: Parcel): TodayStepData? {
            return TodayStepData()
        }

        override fun newArray(size: Int): Array<TodayStepData?> {
            return arrayOfNulls(size)
        }
    }

    fun getDate(): Long {
        return date
    }

    fun setDate(date: Long) {
        this.date = date
    }

    fun getStep(): Long {
        return step
    }

    fun setStep(step: Long) {
        this.step = step
    }

    fun getToday(): String? {
        return today
    }

    fun setToday(today: String?) {
        this.today = today
    }

    override fun toString(): String {
        return "TodayStepData{" +
                ", today=" + today +
                ", date=" + date +
                ", step=" + step +
                '}'
    }
}