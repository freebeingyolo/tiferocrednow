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
    var isDel: String = "0",
    var isFirst: Boolean = true,
    var isFirstOpenApp: Boolean = true,
    var sexLocation: Int = 0,
    var ageLocation: Int = 70,
    var statureLocation: Int = 75,
    var targetWeightLocation: Int = 90,
    var targetStepLocation: Int = 92,
) {
    val setInt: Int get() = if (sex == "男") 0 else 1
    val ageInt: Int get() = age.toInt()
    val statureFloat: Float get() = height.toFloat()
    val targetWeightFloat get() = goalBodyWeight.toFloat()
}

data class StepData(
    var sensorSteps: Int = 0,
    var defaultSteps: Int = 0,
    var todaySteps: Int = 0,
    var saveDate: String = ""
)