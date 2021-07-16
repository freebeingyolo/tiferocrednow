package com.css.service.data

data class UpGradeData(
    val id: Int,
    val isDel: String,
    val mandatoryUpgrade: String,
    val updateContent: String,
    val upgradePackage: String,
    val uploadDate: String,
    val version: String
)