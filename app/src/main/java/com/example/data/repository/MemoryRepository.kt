package com.example.data.repository

import com.example.data.local.MemoryDao
import com.example.data.local.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun insert(memory: MemoryEntity) = memoryDao.insertMemory(memory)

    suspend fun deleteById(id: Int) = memoryDao.deleteMemoryById(id)
}
