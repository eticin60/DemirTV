package com.onurcan.demirtv.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onurcan.demirtv.data.model.Channel
import com.onurcan.demirtv.data.model.Profile
import com.onurcan.demirtv.data.parser.M3UParser
import com.onurcan.demirtv.util.WatchStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels

    private val _filteredChannels = MutableStateFlow<List<Channel>>(emptyList())
    val filteredChannels: StateFlow<List<Channel>> = _filteredChannels

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val parser = M3UParser()
    private val channelSourceUrl = "https://raw.githubusercontent.com/eticin60/DemirTV/master/DemirTV.m3u"
    private val cacheFileName = "channels_cache.m3u"

    private val bannedKeywords = listOf(
        "adult", "18+", "+18", "xxx", "porn", "porno", "pink", "gece", "erotik",
        "sex", "cinsel", "sexy", "hot", "playboy", "night"
    )

    fun selectProfile(profile: Profile) {
        _selectedProfile.value = profile
        fetchChannels()
    }

    private fun fetchChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val fetched = loadChannels()
                        val cleaned = fetched.filter { channel ->
                            val haystack = "${channel.name} ${channel.category} ${channel.streamUrl}".lowercase()
                            bannedKeywords.none { haystack.contains(it) }
                        }

                        val filteredForProfile = if (_selectedProfile.value?.isChild == true) {
                            cleaned.filter { it.isChildSafe }
                        } else {
                            cleaned
                        }

                        filteredForProfile.sortedWith(
                            compareByDescending<Channel> { channel ->
                                WatchStats.getWatchCount(getApplication(), channel)
                            }.thenBy { it.name.lowercase() }
                        )
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                _channels.value = result
                _filteredChannels.value = result
            } catch (e: Exception) {
                _channels.value = emptyList()
                _filteredChannels.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyCategoryFilter(category: String?) {
        // Categories removed as per user request
    }

    private fun loadChannels(): List<Channel> {
        val app = getApplication<Application>()
        return try {
            val text = URL(channelSourceUrl).readText()
            app.openFileOutput(cacheFileName, Application.MODE_PRIVATE).use { it.write(text.toByteArray()) }
            parser.parse(text.byteInputStream())
        } catch (e: Exception) {
            try {
                val cached = app.openFileInput(cacheFileName).use { it.readBytes().toString(Charsets.UTF_8) }
                parser.parse(cached.byteInputStream())
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
