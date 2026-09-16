package com.dron.news.domain.entity

import android.app.Notification


data class Settings(
    val language: Language,
    val interval: Interval,
    val notificationEnabled: Boolean,
    val wifiOnly: Boolean
) {
    companion object {
        val DEFAULT = Settings(
            language = Language.ENGLISH,
            interval = Interval.MIN_15,
            notificationEnabled = false,
            wifiOnly = true
        )
    }
}

enum class Language {
    ENGLISH, RUSSIAN, FRENCH, GERMAN
}

enum class Interval(val minutes: Int) {
    MIN_15(15),
    MIN_30(30),
    HOUR_1(60),
    HOUR_2(120),
    HOUR_4(240),
    HOUR_8(480),
    HOUR_24(1440)
}