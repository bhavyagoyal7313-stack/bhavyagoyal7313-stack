package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DoubtDao {
    @Query("SELECT * FROM doubts ORDER BY timestamp DESC")
    fun getAllDoubts(): Flow<List<DoubtEntity>>

    @Query("SELECT * FROM doubts WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedDoubts(): Flow<List<DoubtEntity>>

    @Query("SELECT * FROM doubts WHERE id = :id LIMIT 1")
    suspend fun getDoubtById(id: Int): DoubtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoubt(doubt: DoubtEntity): Long

    @Update
    suspend fun updateDoubt(doubt: DoubtEntity)

    @Delete
    suspend fun deleteDoubt(doubt: DoubtEntity)

    @Query("DELETE FROM doubts WHERE id = :id")
    suspend fun deleteDoubtById(id: Int)

    @Query("DELETE FROM doubts")
    suspend fun clearAll()
}
