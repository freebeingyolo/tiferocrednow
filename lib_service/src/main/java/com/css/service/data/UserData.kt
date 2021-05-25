package com.css.service.data

data class UserData(
    var sex: String = "男",
    var age: String = "18",
    var stature: String = "170",
    var targetWeight: String = "60",
    var targetStep: String = "10000"
) {
    val setInt: Int get() = if (sex == "男") 0 else 1
    val ageInt: Int get() = age.toInt()
    val statureFloat: Float get() = stature.toFloat()
}

data class StepData(
    var sensorSteps: Int = 0,
    var defaultSteps: Int = 0,
    var todaySteps: Int = 0,
    var saveDate: String = ""
)