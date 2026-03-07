package com.bihe.app.util

object TextUtils {
    
    fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        
        // 中文按字符计算，英文按单词计算
        val chineseChars = text.count { it.code in 0x4E00..0x9FFF }
        val englishWords = text.split(Regex("\\s+"))
            .count { it.isNotBlank() && it.all { it.code !in 0x4E00..0x9FFF } }
        
        return chineseChars + englishWords
    }
    
    fun countCharacters(text: String): Int {
        return text.length
    }
    
    fun truncate(text: String, maxLength: Int): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength) + "..."
    }
    
    fun formatWordCount(count: Int): String {
        return when {
            count < 1000 -> "$count 字"
            count < 10000 -> "${count / 1000}.${(count % 1000) / 100}千字"
            else -> "${count / 10000}.${(count % 10000) / 1000}万字"
        }
    }
    
    fun indentParagraph(text: String): String {
        return text.split("\n")
            .joinToString("\n") { line ->
                if (line.isNotBlank() && !line.startsWith("　") && !line.startsWith(" ")) {
                    "　　$line"
                } else {
                    line
                }
            }
    }
    
    fun removeExtraSpaces(text: String): String {
        return text.replace(Regex(" +"), " ")
            .replace(Regex("　+"), "　")
            .replace(Regex("\n+"), "\n\n")
    }
}
