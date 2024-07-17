package com.example.postapi.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "link_table")
data class Link(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: String,
    val link: String
)