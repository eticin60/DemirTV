package com.onurcan.demirtv.util

import android.content.Context
import com.onurcan.demirtv.data.model.Channel
import java.security.MessageDigest

object WatchStats {
    private const val PREFS = "demirtv_watch_stats"

    fun incrementWatch(context: Context, channel: Channel) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val key = keyFor(channel)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun getWatchCount(context: Context, channel: Channel): Int {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(keyFor(channel), 0)
    }

    private fun keyFor(channel: Channel): String {
        val base = "${channel.name}|${channel.streamUrl}"
        val digest = MessageDigest.getInstance("SHA-256").digest(base.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
