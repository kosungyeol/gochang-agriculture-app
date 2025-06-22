package com.gochang.agriculture.model

data class Project(
    val id: String,
    val category: String, // agriculture, forestry, livestock, fishery
    val name: String,
    val applicationPeriod: String,
    val support1: String,
    val support2: String,
    val target: String,
    val location: String,
    val etc: String,
    var isNotificationEnabled: Boolean = false
) {
    
    fun getCategoryEmoji(): String {
        return when (category) {
            "agriculture" -> "🌾"
            "forestry" -> "🌲"
            "livestock" -> "🐄"
            "fishery" -> "🐟"
            else -> "📋"
        }
    }
    
    fun getCategoryName(): String {
        return when (category) {
            "agriculture" -> "농업"
            "forestry" -> "임업"
            "livestock" -> "축산업"
            "fishery" -> "수산업"
            else -> "기타"
        }
    }
}