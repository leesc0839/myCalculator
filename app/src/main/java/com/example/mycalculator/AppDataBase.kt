package com.example.mycalculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mycalculator.dao.HistoryDao
import com.example.mycalculator.model.History

@Database(entities = [History::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
    // appdataBase 를 통해 HistoryDao 를 가져올 수 있도록 하는 함수
    abstract fun historyDao() : HistoryDao

}