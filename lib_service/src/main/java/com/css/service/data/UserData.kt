package com.css.service.data

data class UserData(
    var sex: String = "男",
    var age: String = "18",
    var stature: String = "170",
    var targetWeight: String = "60",
    var targetStep: String = "10000",
    var sensorSteps: Int = 0,
    var defaultSteps: Int = 0,
    var saveDate: String = "2021年5月12号"
) {
    val setInt: Int = if (sex == "男") 0 else 1
}