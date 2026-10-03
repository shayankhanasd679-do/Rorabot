package com.example.util

import com.example.data.model.BrainRule

data class ParseExportResult(
    val totalLinesParsed: Int,
    val rulesExtracted: List<BrainRule>,
    val detectedKeywords: List<String>,
    val shayanMessageCount: Int,
    val summary: String
)

object ChatExportParser {

    const val SAMPLE_CHAT_EXPORT = """
[02/10/2026, 11:15:20 AM] Bilal Peshawar: Salam Shayan rora!
[02/10/2026, 11:15:45 AM] Shayan: Walaikum Salam rora, sanga ye?
[02/10/2026, 11:16:10 AM] Bilal Peshawar: Kahan ho abhi?
[02/10/2026, 11:16:30 AM] Shayan: Yaar busy yam, tori der baad sms kawam, zr ba gap lagawu
[02/10/2026, 12:40:02 PM] Tariq Hayatabad: Singa chal de rora?
[02/10/2026, 12:40:50 PM] Shayan: Za kha yam rora, ta sanga ye?
[02/10/2026, 03:22:15 PM] Asim University Town: Chai peenay chaltay hain sham ko?
[02/10/2026, 03:23:00 PM] Shayan: Namak Mandi ke chai kho pakka peewu rora!
[02/10/2026, 05:10:12 PM] Farhan Saddar: Files send kar di hain check kar lo.
[02/10/2026, 05:11:00 PM] Shayan: Der der manana rora, zr ba check kram.
"""

    fun parseChatExport(rawText: String): ParseExportResult {
        val lines = rawText.lines()
        val rules = mutableListOf<BrainRule>()
        val pashtoKeywords = mutableSetOf<String>()
        var shayanCount = 0

        var lastFriendMsg: String? = null

        val lineRegex = Regex("""^(?:\[.*?\]\s*|.*?-\s*)(.*?):\s*(.*)$""")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            val match = lineRegex.find(trimmed)
            if (match != null) {
                val sender = match.groupValues[1].trim()
                val message = match.groupValues[2].trim()

                if (sender.contains("Shayan", ignoreCase = true)) {
                    shayanCount++
                    // Check vocabulary
                    listOf("rora", "kha", "busy yam", "tori der baad", "der manana", "zr ba", "sanga ye", "gap lagawu").forEach {
                        if (message.contains(it, ignoreCase = true)) pashtoKeywords.add(it)
                    }

                    if (!lastFriendMsg.isNullOrBlank() && message.isNotBlank()) {
                        val category = when {
                            lastFriendMsg.contains("kahan", true) || lastFriendMsg.contains("cherta", true) -> "ASKING_LOCATION"
                            lastFriendMsg.contains("salam", true) || lastFriendMsg.contains("hello", true) -> "GREETING"
                            lastFriendMsg.contains("singa", true) || lastFriendMsg.contains("kese", true) -> "ASKING_WELLBEING"
                            else -> "GENERAL"
                        }

                        rules.add(
                            BrainRule(
                                triggerPattern = lastFriendMsg,
                                replyText = message,
                                intentCategory = category,
                                source = "CHAT_EXPORT",
                                isActive = true
                            )
                        )
                    }
                    lastFriendMsg = null
                } else {
                    lastFriendMsg = message
                }
            }
        }

        return ParseExportResult(
            totalLinesParsed = lines.size,
            rulesExtracted = rules,
            detectedKeywords = pashtoKeywords.toList(),
            shayanMessageCount = shayanCount,
            summary = "Learned ${rules.size} conversational patterns and ${pashtoKeywords.size} signature Peshawari tone expressions from Shayan's chat history."
        )
    }
}
