package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.model.AutoReplyLog
import com.example.data.model.BlockedContact
import com.example.data.model.BrainRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoraBotRepository(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val blockedDao = db.blockedContactDao()
    private val brainRuleDao = db.brainRuleDao()
    private val logDao = db.autoReplyLogDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("rorabot_settings", Context.MODE_PRIVATE)

    private val _isAutoReplyEnabled = MutableStateFlow(
        prefs.getBoolean("auto_reply_enabled", true)
    )
    val isAutoReplyEnabled: StateFlow<Boolean> = _isAutoReplyEnabled.asStateFlow()

    private val _isWhatsAppConnected = MutableStateFlow(
        prefs.getBoolean("whatsapp_connected", true)
    )
    val isWhatsAppConnected: StateFlow<Boolean> = _isWhatsAppConnected.asStateFlow()

    private val _transcribeVoiceEnabled = MutableStateFlow(
        prefs.getBoolean("transcribe_voice", true)
    )
    val transcribeVoiceEnabled: StateFlow<Boolean> = _transcribeVoiceEnabled.asStateFlow()

    // Blocked contacts
    val allBlocked: Flow<List<BlockedContact>> = blockedDao.getAllBlocked()
    val blockedCount: Flow<Int> = blockedDao.getBlockedCount()

    // Brain rules
    val allBrainRules: Flow<List<BrainRule>> = brainRuleDao.getAllRules()
    val brainRulesCount: Flow<Int> = brainRuleDao.getRulesCount()

    // Reply logs
    val recentLogs: Flow<List<AutoReplyLog>> = logDao.getRecentLogs(30)
    val totalRepliesCount: Flow<Int> = logDao.getSentRepliesCount()
    val vipBlockedEventsCount: Flow<Int> = logDao.getVipBlockedLogsCount()
    val activeChatsCount: Flow<Int> = logDao.getActiveChatsCount()

    suspend fun isNumberBlocked(phoneNumber: String): Boolean {
        val digits = phoneNumber.filter { it.isDigit() }
        if (digits.length < 7) return false
        val targetLast10 = digits.takeLast(10)

        val allBlocked = blockedDao.getAllBlockedList()
        return allBlocked.any { contact ->
            val contactDigits = contact.phoneNumber.filter { it.isDigit() }
            contactDigits.takeLast(10) == targetLast10
        }
    }

    suspend fun addBlockedContact(phone: String, name: String, note: String = "VIP Never Reply"): Long {
        val contact = BlockedContact(
            phoneNumber = phone.trim(),
            name = if (name.isBlank()) "VIP Contact" else name.trim(),
            note = note.trim()
        )
        return blockedDao.insert(contact)
    }

    suspend fun removeBlockedContact(id: Long) {
        blockedDao.deleteById(id)
    }

    suspend fun addBrainRule(pattern: String, reply: String, category: String, source: String = "MANUAL"): Long {
        val rule = BrainRule(
            triggerPattern = pattern.trim(),
            replyText = reply.trim(),
            intentCategory = category,
            source = source,
            isActive = true
        )
        return brainRuleDao.insert(rule)
    }

    suspend fun deleteBrainRule(id: Long) {
        brainRuleDao.deleteById(id)
    }

    suspend fun getActiveRules(): List<BrainRule> {
        return brainRuleDao.getActiveRulesList()
    }

    suspend fun recordLog(log: AutoReplyLog): Long {
        return logDao.insert(log)
    }

    suspend fun clearLogs() {
        logDao.clearAll()
    }

    fun setAutoReplyEnabled(enabled: Boolean) {
        _isAutoReplyEnabled.value = enabled
        prefs.edit().putBoolean("auto_reply_enabled", enabled).apply()
    }

    fun setWhatsAppConnected(connected: Boolean) {
        _isWhatsAppConnected.value = connected
        prefs.edit().putBoolean("whatsapp_connected", connected).apply()
    }

    fun setTranscribeVoiceEnabled(enabled: Boolean) {
        _transcribeVoiceEnabled.value = enabled
        prefs.edit().putBoolean("transcribe_voice", enabled).apply()
    }
}
