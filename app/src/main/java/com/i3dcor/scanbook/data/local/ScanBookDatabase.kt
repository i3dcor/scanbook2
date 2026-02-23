package com.i3dcor.scanbook.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.i3dcor.scanbook.data.local.dao.BookDao
import com.i3dcor.scanbook.data.local.entity.BookEntity

/**
 * Base de datos Room de la aplicación.
 * Singleton con double-checked locking para thread safety.
 */
@Database(entities = [BookEntity::class], version = 2, exportSchema = false)
abstract class ScanBookDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: ScanBookDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE books ADD COLUMN coverLocalPath TEXT DEFAULT NULL")
            }
        }

        fun getInstance(context: Context): ScanBookDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScanBookDatabase::class.java,
                    "scanbook_db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }
    }
}
