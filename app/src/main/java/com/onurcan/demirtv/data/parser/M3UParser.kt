package com.onurcan.demirtv.data.parser

import com.onurcan.demirtv.data.model.Channel
import java.io.InputStream
import java.util.UUID

class M3UParser {
    fun parse(inputStream: InputStream): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = inputStream.bufferedReader()
        var currentName = ""
        var currentLogo = ""
        var currentCategory = "Genel"
        
        reader.forEachLine { line ->
            when {
                line.startsWith("#EXTINF:") -> {
                    // Example: #EXTINF:-1 tvg-id="TRT1.tr" tvg-name="TRT 1" tvg-logo="https://.../trt1.png" group-title="Genel",TRT 1
                    val info = line.substringAfter("#EXTINF:")
                    currentName = info.substringAfter(",").trim()
                    
                    val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(line)
                    currentLogo = logoMatch?.groupValues?.get(1) ?: ""
                    
                    val groupMatch = Regex("""group-title="([^"]+)"""").find(line)
                    currentCategory = groupMatch?.groupValues?.get(1) ?: "Genel"
                }
                line.startsWith("http") -> {
                    if (currentName.isNotEmpty()) {
                        channels.add(
                            Channel(
                                id = UUID.nameUUIDFromBytes("${currentName}|${line.trim()}".toByteArray()).toString(),
                                name = currentName,
                                logoUrl = currentLogo.ifEmpty { null },
                                streamUrl = line.trim(),
                                category = currentCategory,
                                isChildSafe = isKidsCategory(currentCategory)
                            )
                        )
                    }
                    // Reset for next
                    currentName = ""
                    currentLogo = ""
                }
            }
        }
        return channels
    }

    private fun isKidsCategory(category: String): Boolean {
        val kidsKeywords = listOf("çocuk", "kids", "animasyon", "animation", "çizgi", "cartoon", "eğitim", "education")
        return kidsKeywords.any { category.lowercase().contains(it) }
    }
}
