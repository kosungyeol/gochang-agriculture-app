package com.gochang.agriculture.model

import java.util.Date

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
    var isNotificationEnabled: Boolean = false,
    val notificationDate: String? = null,
    val isActive: Boolean = true,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val phone: String? = null,
    val email: String? = null,
    val requirements: String? = null
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
    
    fun getFormattedNotificationText(): String {
        return "${getCategoryEmoji()} $name 사업 신청이 시작되었습니다!\n" +
                "📅 신청기간: $applicationPeriod\n" +
                "💰 지원내용: $support1 $support2\n" +
                "🎯 대상: $target\n" +
                "📍 담당: $location"
    }
}