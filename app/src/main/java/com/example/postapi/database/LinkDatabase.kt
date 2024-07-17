package com.example.postapi.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Link::class], version = 1, exportSchema = false)
abstract class LinkDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao

    companion object {
        @Volatile
        private var INSTANCE: LinkDatabase? = null

        fun getDatabase(context: android.content.Context): LinkDatabase {
            val linkInstance = INSTANCE
            if (linkInstance != null) {
                return linkInstance
            }
            synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    LinkDatabase::class.java,
                    "link_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
