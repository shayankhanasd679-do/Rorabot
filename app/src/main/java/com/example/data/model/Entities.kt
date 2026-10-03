package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_contacts")
data class BlockedContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String, // e.g. "+923005747688"
    val name: String,        // e.g. "VIP Client"
    val note: String = "Messages will stay UNREAD. Double gray tick only.",
    val isSystemPreloaded: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "brain_rules")
data class BrainRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerPattern: String, // "When friend says..."
    val replyText: String,      // "You reply..."
    val intentCategory: String, // "GREETING", "ASKING_LOCATION", "ASKING_WELLBEING", "GENERAL", "CUSTOM"
    val source: String = "MANUAL", // "SYSTEM", "MANUAL", "CHAT_EXPORT", "CORRECTION"
    val isActive: Boolean = true,
    val timesUsed: Int = 0,
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reply_logs")
data class AutoReplyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String,
    val senderNumber: String,
    val incomingText: String,
    val isVoiceNote: Boolean = false,
    val voiceTranscription: String? = null,
    val detectedIntent: String,
    val replyText: String,
    val wasBlocked: Boolean,
    val status: String, // "REPLIED", "BLOCKED_UNREAD", "OFFLINE"
    val timestamp: Long = System.currentTimeMillis()
)
