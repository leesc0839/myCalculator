package com.example.mycalculator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History( // entity 를 통한 db 테이블 생성

    @PrimaryKey val uId: Int?,
    @ColumnInfo(name = "expression") val expression: String?,
    @ColumnInfo(name = "result") val result: String?
)