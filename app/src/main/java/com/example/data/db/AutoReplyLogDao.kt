package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AutoReplyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoReplyLogDao {
    @Query("SELECT * FROM reply_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AutoReplyLog>>

    @Query("SELECT * FROM reply_logs ORDER BY timestamp DESC")
    suspend fun getAllLogsList(): List<AutoReplyLog>

    @Query("SELECT * FROM reply_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<AutoReplyLog>>

    @Query("SELECT COUNT(*) FROM reply_logs WHERE wasBlocked = 0")
    fun getSentRepliesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reply_logs WHERE wasBlocked = 1")
    fun getVipBlockedLogsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reply_logs")
    fun getTotalRepliesCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT senderNumber) FROM reply_logs")
    fun getActiveChatsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AutoReplyLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<AutoReplyLog>)

    @Update
    suspend fun update(log: AutoReplyLog)

    @Query("DELETE FROM reply_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reply_logs")
    suspend fun clearAll()

    @Query("DELETE FROM reply_logs")
    suspend fun deleteAll()
}
