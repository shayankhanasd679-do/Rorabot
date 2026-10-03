package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BlockedContact
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedContactDao {
    @Query("SELECT * FROM blocked_contacts ORDER BY addedTimestamp DESC")
    fun getAllBlocked(): Flow<List<BlockedContact>>

    @Query("SELECT * FROM blocked_contacts")
    suspend fun getAllBlockedList(): List<BlockedContact>

    @Query("SELECT * FROM blocked_contacts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): BlockedContact?

    @Query("SELECT COUNT(*) FROM blocked_contacts")
    fun getBlockedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: BlockedContact): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(contacts: List<BlockedContact>)

    @Update
    suspend fun update(contact: BlockedContact)

    @Delete
    suspend fun delete(contact: BlockedContact)

    @Query("DELETE FROM blocked_contacts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM blocked_contacts")
    suspend fun deleteAll()
}
