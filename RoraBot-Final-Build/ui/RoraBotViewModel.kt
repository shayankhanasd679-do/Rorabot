package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BrainProcessResult
import com.example.ai.RoraBrainEngine
import com.example.data.BackupManager
import com.example.data.RestoreSummary
import com.example.data.db.AppDatabase
import com.example.data.model.AutoReplyLog
import com.example.data.model.BlockedContact
import com.example.data.model.BrainRule
import com.example.data.repository.RoraBotRepository
import com.example.util.ChatExportParser
import com.example.util.ParseExportResult
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SimulatorMessage(
    val id: String,
    val sender: String,
    val text: String,
    val isFromShayanBot: Boolean,
    val isVoiceNote: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val intentTag: String? = null,
    val isRead: Boolean = true, // for VIP blocked, remains unread
    val explanation: String? = null
)

class RoraBotViewModel(application: Application) : AndroidViewModel(application) {
    val repository = RoraBotRepository(application)

    val isAutoReplyEnabled = repository.isAutoReplyEnabled
    val isWhatsAppConnected = repository.isWhatsAppConnected
    val transcribeVoiceEnabled = repository.transcribeVoiceEnabled

    val blockedContacts: StateFlow<List<BlockedContact>> = repository.allBlocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brainRules: StateFlow<List<BrainRule>> = repository.allBrainRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<AutoReplyLog>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRepliesCount: StateFlow<Int> = repository.totalRepliesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val vipBlockedEventsCount: StateFlow<Int> = repository.vipBlockedEventsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val activeChatsCount: StateFlow<Int> = repository.activeChatsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val blockedCount: StateFlow<Int> = repository.blockedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val brainRulesCount: StateFlow<Int> = repository.brainRulesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    // Simulator Chat state
    private val _simulatorMessages = MutableStateFlow<List<SimulatorMessage>>(
        listOf(
            SimulatorMessage(
                id = "m1",
                sender = "Bilal Hayatabad",
                text = "Salam Shayan rora! Kahan ho abhi?",
                isFromShayanBot = false,
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5
            ),
            SimulatorMessage(
                id = "m2",
                sender = "Shayan (RoraBot)",
                text = "Yaar busy yam, tori der baad sms kawam, zr ba gap lagawu",
                isFromShayanBot = true,
                intentTag = "ASKING_LOCATION",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5 + 1200,
                explanation = "100% Intent accuracy: answered location question in Roman Pashto"
            )
        )
    )
    val simulatorMessages: StateFlow<List<SimulatorMessage>> = _simulatorMessages.asStateFlow()

    private val _isBotTyping = MutableStateFlow(false)
    val isBotTyping: StateFlow<Boolean> = _isBotTyping.asStateFlow()

    private val _selectedSimulatorContact = MutableStateFlow("Bilal Hayatabad (+923339123456)")
    val selectedSimulatorContact: StateFlow<String> = _selectedSimulatorContact.asStateFlow()

    // Export parse state
    private val _lastExportResult = MutableStateFlow<ParseExportResult?>(null)
    val lastExportResult: StateFlow<ParseExportResult?> = _lastExportResult.asStateFlow()

    // Log Correction Dialog state
    private val _correctingLog = MutableStateFlow<AutoReplyLog?>(null)
    val correctingLog: StateFlow<AutoReplyLog?> = _correctingLog.asStateFlow()

    fun toggleAutoReply(enabled: Boolean) {
        repository.setAutoReplyEnabled(enabled)
    }

    fun toggleWhatsAppConnection(connected: Boolean) {
        repository.setWhatsAppConnected(connected)
    }

    fun toggleTranscribeVoice(enabled: Boolean) {
        repository.setTranscribeVoiceEnabled(enabled)
    }

    fun selectSimulatorContact(contact: String) {
        _selectedSimulatorContact.value = contact
    }

    fun addBlockedNumber(phone: String, name: String, note: String = "VIP Never Reply") {
        viewModelScope.launch {
            repository.addBlockedContact(phone, name, note)
        }
    }

    fun removeBlockedNumber(id: Long) {
        viewModelScope.launch {
            repository.removeBlockedContact(id)
        }
    }

    fun addBrainRule(pattern: String, reply: String, category: String) {
        viewModelScope.launch {
            repository.addBrainRule(pattern, reply, category, "MANUAL")
        }
    }

    fun deleteBrainRule(id: Long) {
        viewModelScope.launch {
            repository.deleteBrainRule(id)
        }
    }

    fun startCorrectingLog(log: AutoReplyLog) {
        _correctingLog.value = log
    }

    fun dismissCorrection() {
        _correctingLog.value = null
    }

    fun saveLogCorrection(correctedReply: String) {
        val log = _correctingLog.value ?: return
        viewModelScope.launch {
            // Save as high-priority rule in the Brain
            repository.addBrainRule(
                pattern = log.incomingText,
                reply = correctedReply,
                category = log.detectedIntent,
                source = "CORRECTION"
            )
            // Update log entry
            val updated = log.copy(replyText = correctedReply + " (Corrected)")
            repository.recordLog(updated)
            _correctingLog.value = null
        }
    }

    fun parseAndLearnChatExport(text: String) {
        viewModelScope.launch {
            val result = ChatExportParser.parseChatExport(text)
            _lastExportResult.value = result
            for (rule in result.rulesExtracted) {
                repository.addBrainRule(
                    pattern = rule.triggerPattern,
                    reply = rule.replyText,
                    category = rule.intentCategory,
                    source = "CHAT_EXPORT"
                )
            }
        }
    }

    fun clearChatExportResult() {
        _lastExportResult.value = null
    }

    /**
     * Simulates sending a message from a friend into RoraBot
     */
    fun sendSimulatorMessage(
        text: String,
        isVoice: Boolean = false,
        overrideSenderNumber: String? = null,
        overrideSenderName: String? = null
    ) {
        val contactStr = _selectedSimulatorContact.value
        val senderNumber = overrideSenderNumber ?: if (contactStr.contains("3005747688")) {
            "+923005747688"
        } else if (contactStr.contains("3458009193")) {
            "+923458009193"
        } else {
            "+923339123456"
        }
        val senderName = overrideSenderName ?: contactStr.substringBefore(" (")

        val friendMsg = SimulatorMessage(
            id = "user_${System.currentTimeMillis()}",
            sender = senderName,
            text = text,
            isFromShayanBot = false,
            isVoiceNote = isVoice,
            timestamp = System.currentTimeMillis()
        )

        _simulatorMessages.value = _simulatorMessages.value + friendMsg

        viewModelScope.launch {
            // Check if Master switch is OFF
            if (!isAutoReplyEnabled.value) {
                repository.recordLog(
                    AutoReplyLog(
                        senderName = senderName,
                        senderNumber = senderNumber,
                        incomingText = text,
                        isVoiceNote = isVoice,
                        detectedIntent = "SKIPPED",
                        replyText = "[No reply - Auto-reply Master Switch is OFF]",
                        wasBlocked = false,
                        status = "OFFLINE"
                    )
                )
                return@launch
            }

            // Check if contact is on the Never Reply (VIP) List
            val isBlocked = repository.isNumberBlocked(senderNumber)

            if (isBlocked) {
                // Do NOT mark as read. Keep double tick only. No AI reply.
                delay(800)
                repository.recordLog(
                    AutoReplyLog(
                        senderName = senderName,
                        senderNumber = senderNumber,
                        incomingText = text,
                        isVoiceNote = isVoice,
                        detectedIntent = "VIP_NEVER_REPLY",
                        replyText = "[No reply sent - On Never Reply List. Messages stay UNREAD]",
                        wasBlocked = true,
                        status = "BLOCKED_UNREAD"
                    )
                )

                // Add indicator banner in simulator
                val vipNotice = SimulatorMessage(
                    id = "bot_${System.currentTimeMillis()}",
                    sender = "RoraBot Security Guard",
                    text = "🛡️ VIP Filter: $senderNumber is on your Never Reply List.\n• Message stays UNREAD\n• Double gray tick only\n• No AI reply sent.",
                    isFromShayanBot = true,
                    intentTag = "BLOCKED_VIP",
                    isRead = false,
                    explanation = "Never Reply List protection is active."
                )
                _simulatorMessages.value = _simulatorMessages.value + vipNotice
                return@launch
            }

            // Allowed contact: Process through the single Brain
            _isBotTyping.value = true
            delay(1200) // Realistic typing delay

            val activeRules = repository.getActiveRules()
            val textToProcess = if (isVoice) {
                // Transcribe voice note
                val transcript = RoraBrainEngine.transcribeVoiceNote().first
                "$text ($transcript)"
            } else text

            val brainResult: BrainProcessResult = RoraBrainEngine.processMessage(
                incomingMessage = textToProcess,
                senderName = senderName,
                isBlocked = false,
                activeCustomRules = activeRules
            )

            _isBotTyping.value = false

            // Reply to EVERY message (bar bar)
            val botReply = SimulatorMessage(
                id = "bot_${System.currentTimeMillis()}",
                sender = "Shayan (RoraBot)",
                text = brainResult.replyText,
                isFromShayanBot = true,
                intentTag = brainResult.intentCategory,
                isRead = true,
                explanation = brainResult.explanation
            )

            _simulatorMessages.value = _simulatorMessages.value + botReply

            // Record to persistent database log
            repository.recordLog(
                AutoReplyLog(
                    senderName = senderName,
                    senderNumber = senderNumber,
                    incomingText = text,
                    isVoiceNote = isVoice,
                    voiceTranscription = if (isVoice) textToProcess else null,
                    detectedIntent = brainResult.intentCategory,
                    replyText = brainResult.replyText,
                    wasBlocked = false,
                    status = "REPLIED"
                )
            )
        }
    }

    fun clearSimulator() {
        _simulatorMessages.value = emptyList()
    }

    val backupManager = BackupManager(AppDatabase.getInstance(application), repository)

    fun exportBackup(context: Context, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.exportBackup(context)
            onResult(result)
        }
    }

    fun restoreBackup(context: Context, uri: Uri, onResult: (Result<RestoreSummary>) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.restoreBackup(context, uri)
            onResult(result)
        }
    }

    fun resetAllChallengeData(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.resetAllChallengeData()
            onResult(result)
        }
    }

    fun downloadStandaloneZip(context: Context, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            val result = backupManager.downloadStandaloneZip(context)
            onResult(result)
        }
    }
}
