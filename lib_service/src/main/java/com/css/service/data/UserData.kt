package com.css.service.data

data class UserData(
    var sex: String = "男",
    var age: String = "30",
    var height: String = "175",
    var goalBodyWeight: String = "60",
    var goalStepCount: String = "8000",
    var phone: String = "",
    var userName: String = "",
    var userId: Int = 0,
    var pushSet: String = "开",
    var notification: String = "开",
    var isDel: String = "0",
) {
    val sexInt: Int get() = if (sex == "男") 0 else 1
    val ageInt: Int get() = age.toInt()
    val statureFloat: Float get() = height.toFloatOrNull() ?: 175f
    val targetWeightFloat: Float
        get() = goalBodyWeight.toFloatOrNull().let {
            if (it == null || it !in 1f..1000f) {
                goalBodyWeight = "60"
                60f
            }
            else it
        }
    val goalStepCountInt: Int
        get() = goalStepCount.toIntOrNull().let {
            if (it == null || it !in 1000..8000) {
                goalStepCount = "8000"
                8000
            }
            else it
        }
}

data class GlobalData(
    var isFirst: Boolean = true
)

data class StepData(
    var sensorSteps: Int = 0,
    var defaultSteps: Int = 0,
    var todaySteps: Int = 0,
    var saveDate: String = ""
)