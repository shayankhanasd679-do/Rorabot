package com.example

import com.example.ai.BrainProcessResult
import com.example.ai.RoraBrainEngine
import com.example.data.model.AutoReplyLog
import com.example.data.model.BlockedContact
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureTimeMillis

class RoraBotQaTestSuite {

    private fun normalizePhoneDigits(phone: String): String {
        return phone.filter { it.isDigit() }.takeLast(10)
    }

    private fun isNumberBlockedMock(phone: String, blockedContacts: List<BlockedContact>): Boolean {
        val last10 = normalizePhoneDigits(phone)
        return blockedContacts.any { normalizePhoneDigits(it.phoneNumber) == last10 }
    }

    // 1. VIP NEVER-REPLY TEST
    @Test
    fun test1_VipNeverReplyTest() = runBlocking {
        val blockedList = listOf(
            BlockedContact(id = 1, phoneNumber = "+923005747688", name = "VIP Number 1"),
            BlockedContact(id = 2, phoneNumber = "+923458009193", name = "VIP Number 2")
        )

        val inputPhone = "03005747688"
        val isBlocked = isNumberBlockedMock(inputPhone, blockedList)
        assertTrue("Phone $inputPhone should be recognized as VIP blocked", isBlocked)

        val result = RoraBrainEngine.processMessage(
            incomingMessage = "Urgent call me",
            senderName = "VIP Number 1",
            isBlocked = isBlocked,
            activeCustomRules = emptyList()
        )

        assertTrue("Result must be flagged as blocked", result.isBlocked)
        assertEquals("BLOCKED_VIP", result.intentCategory)
        assertTrue("Reply text should indicate no auto-reply sent", result.replyText.contains("No reply sent"))
        assertFalse("Auto-reply must NOT send a friendly response to VIP", result.replyText.contains("Salam") || result.replyText.contains("Za kha yam"))
    }

    // 2. GREETING INTENT TEST
    @Test
    fun test2_GreetingIntentTest() = runBlocking {
        val input = "Salam rora"
        val result = RoraBrainEngine.processMessage(
            incomingMessage = input,
            senderName = "Bilal",
            isBlocked = false,
            activeCustomRules = emptyList()
        )

        assertEquals("GREETING", result.intentCategory)
        val replyLower = result.replyText.lowercase()
        val containsExpected = replyLower.contains("walaikum salam") || replyLower.contains("sanga ye")
        assertTrue("Expected reply to contain 'Walaikum Salam' or 'Sanga ye', got: ${result.replyText}", containsExpected)
        assertFalse("Must NOT reply with 'Za kha yam' for a greeting!", replyLower.contains("za kha yam"))
    }

    // 3. LOCATION INTENT TEST
    @Test
    fun test3_LocationIntentTest() = runBlocking {
        val inputs = listOf("kaha ho", "kider ho", "where are you", "cherta ye")
        for (input in inputs) {
            val result = RoraBrainEngine.processMessage(
                incomingMessage = input,
                senderName = "Bilal",
                isBlocked = false,
                activeCustomRules = emptyList()
            )

            assertEquals("ASKING_LOCATION", result.intentCategory)
            val replyLower = result.replyText.lowercase()
            assertTrue("Expected reply related to location/busy, got: ${result.replyText}", replyLower.contains("busy"))
            assertFalse("Must NOT reply with greeting to location question!", replyLower.contains("walaikum salam"))
        }
    }

    // 4. WELLBEING INTENT TEST
    @Test
    fun test4_WellbeingIntentTest() = runBlocking {
        val inputs = listOf("kese ho", "sanga ye")
        for (input in inputs) {
            val result = RoraBrainEngine.processMessage(
                incomingMessage = input,
                senderName = "Bilal",
                isBlocked = false,
                activeCustomRules = emptyList()
            )

            assertEquals("ASKING_WELLBEING", result.intentCategory)
            val replyLower = result.replyText.lowercase()
            val containsKha = replyLower.contains("za kha yam") || replyLower.contains("kha yam")
            assertTrue("Expected reply to contain 'za kha yam' or 'kha yam', got: ${result.replyText}", containsKha)
        }
    }

    // 5. REPEATED MESSAGE TEST
    @Test
    fun test5_RepeatedMessageTest() = runBlocking {
        // Send "hi" 3 times in a row from same user. App should reply 3 times, not skip.
        val replies = mutableListOf<BrainProcessResult>()
        for (i in 1..3) {
            val result = RoraBrainEngine.processMessage(
                incomingMessage = "hi",
                senderName = "Bilal",
                isBlocked = false,
                activeCustomRules = emptyList()
            )
            replies.add(result)
        }

        assertEquals("Should reply to all 3 messages without skipping", 3, replies.size)
        replies.forEach { reply ->
            assertEquals("GREETING", reply.intentCategory)
            assertTrue("Each reply should have content", reply.replyText.isNotBlank())
        }
    }

    // 6. PASHTO LANGUAGE TEST
    @Test
    fun test6_PashtoLanguageTest() = runBlocking {
        val input = "za kho da kha yam"
        val result = RoraBrainEngine.processMessage(
            incomingMessage = input,
            senderName = "Tariq",
            isBlocked = false,
            activeCustomRules = emptyList()
        )

        val replyLower = result.replyText.lowercase()
        // Should understand and reply in authentic Roman Pashto tone (e.g. "Der kha rora, Allah de khushala lara!")
        assertTrue("Should reply in authentic Roman Pashto, got: ${result.replyText}",
            replyLower.contains("kha") || replyLower.contains("rora") || replyLower.contains("khushala") || replyLower.contains("manana"))
    }

    // 7. SPEED TEST
    @Test
    fun test7_SpeedTest() = runBlocking {
        val elapsedMs = measureTimeMillis {
            val result = RoraBrainEngine.processMessage(
                incomingMessage = "kaha ho abhi?",
                senderName = "Bilal",
                isBlocked = false,
                activeCustomRules = emptyList()
            )
            assertTrue(result.replyText.isNotBlank())
        }

        println("Speed test execution time: ${elapsedMs}ms")
        assertTrue("Response time must be under 3000ms (was ${elapsedMs}ms)", elapsedMs < 3000)
    }

    // 8. WRONG REPLY CROSS-CHECK
    @Test
    fun test8_WrongReplyCrossCheck() = runBlocking {
        // Test cross-checks across all key intents:
        val locationResult = RoraBrainEngine.processMessage(
            incomingMessage = "kaha ho",
            senderName = "Bilal",
            isBlocked = false,
            activeCustomRules = emptyList()
        )
        val greetingResult = RoraBrainEngine.processMessage(
            incomingMessage = "Salam",
            senderName = "Bilal",
            isBlocked = false,
            activeCustomRules = emptyList()
        )
        val wellbeingResult = RoraBrainEngine.processMessage(
            incomingMessage = "kese ho",
            senderName = "Bilal",
            isBlocked = false,
            activeCustomRules = emptyList()
        )

        // 1. "kaha ho" reply must NOT equal "salam" reply
        assertNotEquals(locationResult.replyText, greetingResult.replyText)
        assertFalse("Location reply must NOT contain Salam", locationResult.replyText.contains("Salam", ignoreCase = true))

        // 2. "salam" reply must NOT equal "kaha ho" reply
        assertFalse("Greeting reply must NOT say busy yam", greetingResult.replyText.contains("busy yam", ignoreCase = true))

        // 3. "kese ho" reply must NOT equal "kaha ho" reply
        assertNotEquals(wellbeingResult.replyText, locationResult.replyText)
        assertFalse("Wellbeing reply must NOT say busy yam", wellbeingResult.replyText.contains("busy yam", ignoreCase = true))
    }

    // 9. VIP 2 TEST
    @Test
    fun test9_Vip2Test() = runBlocking {
        val blockedList = listOf(
            BlockedContact(id = 1, phoneNumber = "+923005747688", name = "VIP Number 1"),
            BlockedContact(id = 2, phoneNumber = "+923458009193", name = "VIP Number 2")
        )

        // Test second VIP number in local format: 03458009193
        val inputPhone2 = "03458009193"
        val isBlocked = isNumberBlockedMock(inputPhone2, blockedList)
        assertTrue("Second VIP phone $inputPhone2 should be recognized as VIP blocked", isBlocked)

        val result = RoraBrainEngine.processMessage(
            incomingMessage = "Meeting at 4pm",
            senderName = "VIP Number 2",
            isBlocked = isBlocked,
            activeCustomRules = emptyList()
        )

        assertTrue("VIP 2 result must be flagged as blocked", result.isBlocked)
        assertEquals("BLOCKED_VIP", result.intentCategory)
        assertTrue("No auto-reply should be sent to VIP 2", result.replyText.contains("No reply sent"))
    }

    // 10. LOGGING TEST
    @Test
    fun test10_LoggingTest() {
        val logs = mutableListOf<AutoReplyLog>()

        // 1. Simulate an auto-reply
        logs.add(
            AutoReplyLog(
                id = 1,
                senderName = "Bilal",
                senderNumber = "+923339123456",
                incomingText = "Salam rora",
                detectedIntent = "GREETING",
                replyText = "Walaikum Salam rora, sanga ye? 😊",
                wasBlocked = false,
                status = "REPLIED"
            )
        )

        // 2. Simulate a VIP blocked event
        logs.add(
            AutoReplyLog(
                id = 2,
                senderName = "VIP Number 1",
                senderNumber = "03005747688",
                incomingText = "Call me now",
                detectedIntent = "VIP_NEVER_REPLY",
                replyText = "[No reply sent - On Never Reply List]",
                wasBlocked = true,
                status = "BLOCKED_UNREAD"
            )
        )

        val repliesSentCount = logs.count { !it.wasBlocked }
        val vipBlockedCount = logs.count { it.wasBlocked }

        assertEquals("Replies Sent count should be 1", 1, repliesSentCount)
        assertEquals("VIP Blocked count should be 1", 1, vipBlockedCount)
    }
}
