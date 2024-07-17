package com.example.postapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LinkDao {
    @Insert
    suspend fun insert(link: Link)

    @Query("SELECT * FROM link_table ORDER BY timestamp DESC")
    fun getAllLinks(): List<Link>
}