package com.css.service.data


data class LoginUserData(
    var token: String="",
    var userInfo: UserInfo
)
data class UserInfo(
    var age: String="",
    var goalBodyWeight: String="",
    var goalStepCount: String="",
    var height: String="",
    var isDel: String="",
    var lastLoginTime: String="",
    var password: String="",
    var phone: String="",
    val productFivesId: Any,
    val productFourId: Any,
    val productOneId: Any,
    val productThreeId: Any,
    val productTwoId: Any,
    var pushSet: String="",
    var sex: String="",
    var userId: Int=-1,
    var userName: String="",
    var userRegisterTime: String="",
)