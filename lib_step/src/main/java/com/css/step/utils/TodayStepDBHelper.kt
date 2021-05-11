package com.css.step.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.css.step.data.TodayStepData
import java.util.*

class TodayStepDBHelper: SQLiteOpenHelper {
    private val TAG = "TodayStepDBHelper"

    private val TABLE_NAME = "TodayStepData"
    private val PRIMARY_KEY = "_id"
    val TODAY = "today"
    val DATE = "date"
    val STEP = "step"

    private val SQL_CREATE_TABLE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + PRIMARY_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TODAY + " TEXT, "
            + DATE + " long, "
            + STEP + " long);")
    private val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    private val SQL_QUERY_ALL = "SELECT * FROM $TABLE_NAME"
    private val SQL_QUERY_STEP =
        "SELECT * FROM $TABLE_NAME WHERE $TODAY = ? AND $STEP = ?"

    constructor(context: Context) : super(context, "TodayStepDB.db", null, 1)


    override fun onCreate(db: SQLiteDatabase) {
        Logger().e(TAG, SQL_CREATE_TABLE)
        db.execSQL(SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        deleteTable()
        onCreate(db)
    }

    @Synchronized
    fun isExist(todayStepData: TodayStepData): Boolean {
        val cursor: Cursor = getReadableDatabase().rawQuery(
            SQL_QUERY_STEP,
            arrayOf(todayStepData.getToday(), todayStepData.getStep().toString() + "")
        )
        val exist = if (cursor.count > 0) true else false
        cursor.close()
        return exist
    }

    @Synchronized
    fun createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE)
    }

    @Synchronized
    fun insert(todayStepData: TodayStepData) {
        val contentValues = ContentValues()
        contentValues.put(TODAY, todayStepData.getToday())
        contentValues.put(DATE, todayStepData.getDate())
        contentValues.put(STEP, todayStepData.getStep())
        getWritableDatabase().insert(TABLE_NAME, null, contentValues)
    }

    @Synchronized
    fun getQueryAll(): List<TodayStepData>? {
        val cursor: Cursor = getReadableDatabase().rawQuery(SQL_QUERY_ALL, arrayOf<String>())
        val todayStepDatas: MutableList<TodayStepData> = ArrayList()
        while (cursor.moveToNext()) {
            val today = cursor.getString(cursor.getColumnIndex(TODAY))
            val date = cursor.getLong(cursor.getColumnIndex(DATE))
            val step = cursor.getLong(cursor.getColumnIndex(STEP))
            val todayStepData = TodayStepData()
            todayStepData.setToday(today)
            todayStepData.setDate(date)
            todayStepData.setStep(step)
            todayStepDatas.add(todayStepData)
        }
        cursor.close()
        return todayStepDatas
    }

    @Synchronized
    fun deleteTable() {
        getWritableDatabase().execSQL(SQL_DELETE_TABLE)
    }
}