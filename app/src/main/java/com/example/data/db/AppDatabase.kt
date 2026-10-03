package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AutoReplyLog
import com.example.data.model.BlockedContact
import com.example.data.model.BrainRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BlockedContact::class,
        BrainRule::class,
        AutoReplyLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blockedContactDao(): BlockedContactDao
    abstract fun brainRuleDao(): BrainRuleDao
    abstract fun autoReplyLogDao(): AutoReplyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rorabot_peshawar.db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate required default contacts and core Brain intent rules
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            populateInitialData(database)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // 1. Mandatory Pre-added numbers from specifications:
            val preloadedContacts = listOf(
                BlockedContact(
                    phoneNumber = "+923005747688",
                    name = "VIP Number 1",
                    note = "Preloaded VIP: Never auto-reply. Double tick only, stays unread.",
                    isSystemPreloaded = true
                ),
                BlockedContact(
                    phoneNumber = "+923458009193",
                    name = "VIP Number 2",
                    note = "Preloaded VIP: Never auto-reply. Double tick only, stays unread.",
                    isSystemPreloaded = true
                )
            )
            db.blockedContactDao().insertAll(preloadedContacts)

            // 2. Initial core rules for Shayan's Roman Pashto Brain
            val initialRules = listOf(
                BrainRule(
                    triggerPattern = "hi, hello, salam, assalam o alaikum, ao rora",
                    replyText = "Walaikum Salam rora, sanga ye? 😊",
                    intentCategory = "GREETING",
                    source = "SYSTEM"
                ),
                BrainRule(
                    triggerPattern = "kider ho, kaha ho, cherta ye, where are you, chata ye, charta ye",
                    replyText = "Yaar busy yam, tori der baad sms kawam, zr ba gap lagawu",
                    intentCategory = "ASKING_LOCATION",
                    source = "SYSTEM"
                ),
                BrainRule(
                    triggerPattern = "kese ho, singa ye, how are you, sa hal de, theek ho, kha ye",
                    replyText = "Za kha yam rora, ta sanga ye?",
                    intentCategory = "ASKING_WELLBEING",
                    source = "SYSTEM"
                ),
                BrainRule(
                    triggerPattern = "chai piyo ge, chai khwro, chai cherta ye",
                    replyText = "Namak Mandi ke chai kho pakka peewu rora, kho da kar khatam krram!",
                    intentCategory = "GENERAL",
                    source = "SYSTEM"
                ),
                BrainRule(
                    triggerPattern = "peshawar ke ho, kahan ho peshawar",
                    replyText = "Awo rora, Hayatabad k yama, tori der baad fone kawama.",
                    intentCategory = "ASKING_LOCATION",
                    source = "SYSTEM"
                )
            )
            db.brainRuleDao().insertAll(initialRules)

            // 3. Initial sample log so user sees active state
            val sampleLogs = listOf(
                AutoReplyLog(
                    senderName = "Bilal Peshawar",
                    senderNumber = "+923339123456",
                    incomingText = "Salam rora!",
                    isVoiceNote = false,
                    detectedIntent = "GREETING",
                    replyText = "Walaikum Salam rora, sanga ye? 😊",
                    wasBlocked = false,
                    status = "REPLIED",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 18
                ),
                AutoReplyLog(
                    senderName = "VIP Number 1",
                    senderNumber = "+923005747688",
                    incomingText = "Urgent call me",
                    isVoiceNote = false,
                    detectedIntent = "GENERAL",
                    replyText = "[No reply sent - On Never Reply List]",
                    wasBlocked = true,
                    status = "BLOCKED_UNREAD",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 42
                )
            )
            for (log in sampleLogs) {
                db.autoReplyLogDao().insert(log)
            }
        }
    }
}
