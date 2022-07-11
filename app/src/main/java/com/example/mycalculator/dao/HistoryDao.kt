package com.example.mycalculator.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mycalculator.model.History

@Dao // Dao -> room 에 연결되었음
interface HistoryDao {
    @Query("SELECT * FROM history") // query 문 작성을 통해 데이터 관리
    fun getAllHistory(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history")
    fun deleteAllHistory()
}