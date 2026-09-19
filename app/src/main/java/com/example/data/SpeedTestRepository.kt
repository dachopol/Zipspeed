package com.example.data

import kotlinx.coroutines.flow.Flow

class SpeedTestRepository(private val dao: SpeedTestDao) {
    val allRecords: Flow<List<SpeedTestRecord>> = dao.getAllRecords()

    suspend fun insertRecord(record: SpeedTestRecord) {
        dao.insertRecord(record)
    }

    suspend fun deleteRecord(record: SpeedTestRecord) {
        dao.deleteRecord(record)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
