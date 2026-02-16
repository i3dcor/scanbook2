package com.i3dcor.scanbook.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.i3dcor.scanbook.data.local.dao.BookDao
import com.i3dcor.scanbook.data.local.entity.BookEntity

/**
 * Base de datos Room de la aplicación.
 * Singleton con double-checked locking para thread safety.
 */
@Database(entities = [BookEntity::class], version = 1, exportSchema = false)
abstract class ScanBookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: ScanBookDatabase? = null

        fun getInstance(context: Context): ScanBookDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScanBookDatabase::class.java,
                    "scanbook_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
