package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BrainRule
import kotlinx.coroutines.flow.Flow

@Dao
interface BrainRuleDao {
    @Query("SELECT * FROM brain_rules ORDER BY createdTimestamp DESC")
    fun getAllRules(): Flow<List<BrainRule>>

    @Query("SELECT * FROM brain_rules")
    suspend fun getAllRulesList(): List<BrainRule>

    @Query("SELECT * FROM brain_rules WHERE isActive = 1")
    suspend fun getActiveRulesList(): List<BrainRule>

    @Query("SELECT COUNT(*) FROM brain_rules")
    fun getRulesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: BrainRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<BrainRule>)

    @Update
    suspend fun update(rule: BrainRule)

    @Delete
    suspend fun delete(rule: BrainRule)

    @Query("DELETE FROM brain_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE brain_rules SET timesUsed = timesUsed + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("DELETE FROM brain_rules")
    suspend fun deleteAll()
}
