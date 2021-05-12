package com.css.step.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by chenPan
 * Date: 2021/5/11
 * To do:
 */
@Entity(tableName = "StepEntity")
data class StepEntity (
    @PrimaryKey(autoGenerate = true)
    var curDate: String? = null, // 当天的日期
    @ColumnInfo(name = "s_name")
    var steps: String? = null   // 存入系统计步器的步数
)