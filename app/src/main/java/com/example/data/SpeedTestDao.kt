package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {
    @Query("SELECT * FROM speed_test_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<SpeedTestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SpeedTestRecord)

    @Delete
    suspend fun deleteRecord(record: SpeedTestRecord)

    @Query("DELETE FROM speed_test_records")
    suspend fun clearAll()
}
