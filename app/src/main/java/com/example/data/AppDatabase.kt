package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SpeedTestRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun speedTestDao(): SpeedTestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE speed_test_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        downloadMbps REAL NOT NULL,
                        uploadMbps REAL NOT NULL,
                        pingMs INTEGER NOT NULL,
                        jitterMs INTEGER NOT NULL,
                        packetLossPercent REAL,
                        serverName TEXT NOT NULL,
                        serverLocation TEXT NOT NULL,
                        networkType TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO speed_test_records_new
                    (id, timestamp, downloadMbps, uploadMbps, pingMs, jitterMs, packetLossPercent, serverName, serverLocation, networkType)
                    SELECT id, timestamp, downloadMbps, uploadMbps, pingMs, jitterMs, packetLossPercent, serverName, serverLocation, networkType
                    FROM speed_test_records
                """.trimIndent())
                db.execSQL("DROP TABLE speed_test_records")
                db.execSQL("ALTER TABLE speed_test_records_new RENAME TO speed_test_records")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zipspeed_database"
                ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
