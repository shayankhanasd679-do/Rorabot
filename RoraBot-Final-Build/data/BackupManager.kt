package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.room.withTransaction
import com.example.data.db.AppDatabase
import com.example.data.model.AutoReplyLog
import com.example.data.model.BlockedContact
import com.example.data.model.BrainRule
import com.example.data.repository.RoraBotRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RestoreSummary(
    val brainRulesCount: Int,
    val blockedCount: Int,
    val logsCount: Int
)

class BackupManager(
    private val db: AppDatabase,
    private val repository: RoraBotRepository
) {
    private val brainRuleDao = db.brainRuleDao()
    private val blockedDao = db.blockedContactDao()
    private val logDao = db.autoReplyLogDao()

    suspend fun exportBackup(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val rules = brainRuleDao.getAllRulesList()
            val blocked = blockedDao.getAllBlockedList()
            val logs = logDao.getAllLogsList()

            val rootJson = JSONObject()

            // Brain Rules
            val rulesArray = JSONArray()
            for (rule in rules) {
                val obj = JSONObject().apply {
                    put("triggerPattern", rule.triggerPattern)
                    put("replyText", rule.replyText)
                    put("intentCategory", rule.intentCategory)
                    put("source", rule.source)
                    put("isActive", rule.isActive)
                    put("timesUsed", rule.timesUsed)
                    put("createdTimestamp", rule.createdTimestamp)
                }
                rulesArray.put(obj)
            }
            rootJson.put("brainRules", rulesArray)

            // Blocked Numbers
            val blockedArray = JSONArray()
            for (contact in blocked) {
                val obj = JSONObject().apply {
                    put("phoneNumber", contact.phoneNumber)
                    put("name", contact.name)
                    put("note", contact.note)
                    put("isSystemPreloaded", contact.isSystemPreloaded)
                    put("addedTimestamp", contact.addedTimestamp)
                }
                blockedArray.put(obj)
            }
            rootJson.put("blockedNumbers", blockedArray)

            // Logs
            val logsArray = JSONArray()
            for (log in logs) {
                val obj = JSONObject().apply {
                    put("senderName", log.senderName)
                    put("senderNumber", log.senderNumber)
                    put("incomingText", log.incomingText)
                    put("isVoiceNote", log.isVoiceNote)
                    put("voiceTranscription", log.voiceTranscription ?: "")
                    put("detectedIntent", log.detectedIntent)
                    put("replyText", log.replyText)
                    put("wasBlocked", log.wasBlocked)
                    put("status", log.status)
                    put("timestamp", log.timestamp)
                }
                logsArray.put(obj)
            }
            rootJson.put("logs", logsArray)

            // Preferences
            val prefsObj = JSONObject().apply {
                put("autoReplyEnabled", repository.isAutoReplyEnabled.value)
                put("whatsAppConnected", repository.isWhatsAppConnected.value)
                put("transcribeVoiceEnabled", repository.transcribeVoiceEnabled.value)
                put("version", "1.0.0")
                put("exportTimestamp", System.currentTimeMillis())
            }
            rootJson.put("preferences", prefsObj)

            val jsonString = rootJson.toString(2)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val fileName = "RoraBot-Brain-Backup-$dateStr.json"

            var writeSuccess = false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    writeSuccess = true
                }
            }

            if (!writeSuccess) {
                // Direct file write fallback
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                targetFile.writeText(jsonString, Charsets.UTF_8)
            }

            Log.i("BackupManager", "Export successful: $fileName with ${rules.size} rules, ${blocked.size} blocked, ${logs.size} logs")
            Result.success(fileName)
        } catch (e: Exception) {
            Log.e("BackupManager", "Export failed", e)
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(context: Context, uri: Uri): Result<RestoreSummary> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: throw IllegalStateException("Could not read backup file content")

            val rootJson = JSONObject(jsonString)

            val restoredRules = mutableListOf<BrainRule>()
            val rulesArray = rootJson.optJSONArray("brainRules")
            if (rulesArray != null) {
                for (i in 0 until rulesArray.length()) {
                    val obj = rulesArray.getJSONObject(i)
                    restoredRules.add(
                        BrainRule(
                            triggerPattern = obj.getString("triggerPattern"),
                            replyText = obj.getString("replyText"),
                            intentCategory = obj.optString("intentCategory", "GENERAL"),
                            source = obj.optString("source", "MANUAL"),
                            isActive = obj.optBoolean("isActive", true),
                            timesUsed = obj.optInt("timesUsed", 0),
                            createdTimestamp = obj.optLong("createdTimestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val restoredBlocked = mutableListOf<BlockedContact>()
            val blockedArray = rootJson.optJSONArray("blockedNumbers")
            if (blockedArray != null) {
                for (i in 0 until blockedArray.length()) {
                    val obj = blockedArray.getJSONObject(i)
                    restoredBlocked.add(
                        BlockedContact(
                            phoneNumber = obj.getString("phoneNumber"),
                            name = obj.optString("name", "VIP Contact"),
                            note = obj.optString("note", "VIP Never Reply"),
                            isSystemPreloaded = obj.optBoolean("isSystemPreloaded", false),
                            addedTimestamp = obj.optLong("addedTimestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val restoredLogs = mutableListOf<AutoReplyLog>()
            val logsArray = rootJson.optJSONArray("logs")
            if (logsArray != null) {
                for (i in 0 until logsArray.length()) {
                    val obj = logsArray.getJSONObject(i)
                    restoredLogs.add(
                        AutoReplyLog(
                            senderName = obj.optString("senderName", "Friend"),
                            senderNumber = obj.optString("senderNumber", ""),
                            incomingText = obj.optString("incomingText", ""),
                            isVoiceNote = obj.optBoolean("isVoiceNote", false),
                            voiceTranscription = obj.optString("voiceTranscription").ifBlank { null },
                            detectedIntent = obj.optString("detectedIntent", "GENERAL"),
                            replyText = obj.optString("replyText", ""),
                            wasBlocked = obj.optBoolean("wasBlocked", false),
                            status = obj.optString("status", "REPLIED"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val prefsObj = rootJson.optJSONObject("preferences")
            if (prefsObj != null) {
                if (prefsObj.has("autoReplyEnabled")) {
                    repository.setAutoReplyEnabled(prefsObj.getBoolean("autoReplyEnabled"))
                }
                if (prefsObj.has("whatsAppConnected")) {
                    repository.setWhatsAppConnected(prefsObj.getBoolean("whatsAppConnected"))
                }
                if (prefsObj.has("transcribeVoiceEnabled")) {
                    repository.setTranscribeVoiceEnabled(prefsObj.getBoolean("transcribeVoiceEnabled"))
                }
            }

            db.withTransaction {
                brainRuleDao.deleteAll()
                blockedDao.deleteAll()
                logDao.deleteAll()

                if (restoredRules.isNotEmpty()) {
                    brainRuleDao.insertAll(restoredRules)
                }
                if (restoredBlocked.isNotEmpty()) {
                    blockedDao.insertAll(restoredBlocked)
                }
                if (restoredLogs.isNotEmpty()) {
                    logDao.insertAll(restoredLogs)
                }
            }

            val summary = RestoreSummary(
                brainRulesCount = restoredRules.size,
                blockedCount = restoredBlocked.size,
                logsCount = restoredLogs.size
            )
            Log.i("BackupManager", "Restore successful: $summary")
            Result.success(summary)
        } catch (e: Exception) {
            Log.e("BackupManager", "Restore failed", e)
            Result.failure(e)
        }
    }

    suspend fun resetAllChallengeData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                brainRuleDao.deleteAll()
                blockedDao.deleteAll()
                logDao.deleteAll()
            }
            repository.setAutoReplyEnabled(true)
            repository.setWhatsAppConnected(true)
            repository.setTranscribeVoiceEnabled(true)

            Log.i("BackupManager", "All challenge data reset successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BackupManager", "Reset failed", e)
            Result.failure(e)
        }
    }

    suspend fun downloadStandaloneZip(context: Context): Result<String> = withContext(Dispatchers.IO) {
        val fileName = "RoraBot-FINAL-DOWNLOADABLE.zip"
        try {
            var inputStream: InputStream? = null

            // 1. Try reading from APK assets
            try {
                inputStream = context.assets.open(fileName)
            } catch (e: Exception) {
                Log.w("BackupManager", "Asset not found, checking local files", e)
            }

            // 2. Try reading from workspace root if available
            if (inputStream == null) {
                val rootZip = File("/$fileName")
                if (rootZip.exists() && rootZip.canRead()) {
                    inputStream = rootZip.inputStream()
                } else {
                    val appletZip = File("/app/applet/$fileName")
                    if (appletZip.exists() && appletZip.canRead()) {
                        inputStream = appletZip.inputStream()
                    }
                }
            }

            if (inputStream == null) {
                return@withContext Result.failure(IllegalStateException("ZIP file not found in package"))
            }

            var savedUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        inputStream.copyTo(out)
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    savedUri = uri
                }
            }

            if (savedUri == null) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    inputStream.copyTo(out)
                }
                savedUri = Uri.fromFile(targetFile)
            }

            inputStream.close()
            Log.i("BackupManager", "ZIP exported successfully to Downloads: $savedUri")
            Result.success(fileName)
        } catch (e: Exception) {
            Log.e("BackupManager", "ZIP export failed", e)
            Result.failure(e)
        }
    }
}
