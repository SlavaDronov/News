package com.dron.news.data.mapper

import com.dron.news.domain.entity.RefreshConfig
import com.dron.news.domain.entity.Settings

fun Settings.toRefreshConfig(): RefreshConfig {
    return RefreshConfig(language, interval, wifiOnly)
}