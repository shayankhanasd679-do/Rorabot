package com.example

import com.example.ai.RoraBrainEngine
import com.example.util.ChatExportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testIntentClassification() {
        // ASKING_LOCATION
        assertTrue(RoraBrainEngine.isAskingLocation("kaha ho abhi?"))
        assertTrue(RoraBrainEngine.isAskingLocation("cherta ye rora?"))
        assertTrue(RoraBrainEngine.isAskingLocation("where are you"))

        // ASKING_WELLBEING
        assertTrue(RoraBrainEngine.isAskingWellbeing("kese ho bhai?"))
        assertTrue(RoraBrainEngine.isAskingWellbeing("singa ye?"))

        // GREETING
        assertTrue(RoraBrainEngine.isGreeting("salam"))
        assertTrue(RoraBrainEngine.isGreeting("assalam o alaikum"))
        assertTrue(RoraBrainEngine.isGreeting("hello"))
    }

    @Test
    fun testChatExportParser() {
        val result = ChatExportParser.parseChatExport(ChatExportParser.SAMPLE_CHAT_EXPORT)
        assertTrue(result.rulesExtracted.isNotEmpty())
        assertTrue(result.detectedKeywords.contains("rora"))
    }
}
