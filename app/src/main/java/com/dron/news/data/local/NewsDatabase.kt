package com.dron.news.data.local

import android.content.Context
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [SubscriptionDbModel::class, ArticleDbModel::class],
    version = 1,
    exportSchema = false)

abstract class NewsDatabase : RoomDatabase() {

    abstract fun newDao(): NewDao

}
