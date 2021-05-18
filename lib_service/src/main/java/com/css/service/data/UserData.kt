package com.css.service.data

data class UserData(
    var sex: String="男",
    var age: String="",
    var stature: String="",
    var targetWeight: String="",
    var targetStep: String=""
) {
    val setInt: Int = if (sex == "男") 0 else 1
}

data class StepData(
    var sensorSteps: Int=0,
    var defaultSteps: Int=0,
    var saveDate: String=""
)