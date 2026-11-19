package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameRunEntity::class], version = 1, exportSchema = false)
abstract class SnakeDatabase : RoomDatabase() {
  abstract fun gameRunDao(): GameRunDao

  companion object {
    @Volatile private var INSTANCE: SnakeDatabase? = null

    fun getInstance(context: Context): SnakeDatabase {
      return INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                SnakeDatabase::class.java,
                "pocket_snake_db",
              )
              .build()
              .also { INSTANCE = it }
        }
    }
  }
}
