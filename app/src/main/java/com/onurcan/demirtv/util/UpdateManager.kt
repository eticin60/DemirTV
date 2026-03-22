package com.onurcan.demirtv.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.onurcan.demirtv.BuildConfig
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

object UpdateManager {
    // GitHub Raw URL for update.json
    private const val UPDATE_URL = "https://raw.githubusercontent.com/eticin60/DemirTV/master/update.json"

    @Composable
    fun CheckForUpdates(context: Context) {
        var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        
        val currentVersionCode = BuildConfig.VERSION_CODE

        LaunchedEffect(Unit) {
            val info = fetchUpdateInfo()
            if (info != null && info.versionCode > currentVersionCode) {
                updateInfo = info
                showDialog = true
            }
        }

        if (showDialog && updateInfo != null) {
            AlertDialog(
                onDismissRequest = {
                    if (!updateInfo!!.forceUpdate) showDialog = false
                },
                title = { Text("Yeni Sürüm Hazır") },
                text = { Text("DemirTV v${updateInfo!!.versionName} sürümü çıktı. Yeni özellikler ve performans iyileştirmeleri için güncelleyin.") },
                confirmButton = {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo!!.downloadUrl))
                        context.startActivity(intent)
                        if (!updateInfo!!.forceUpdate) showDialog = false
                    }) {
                        Text("ŞİMDİ GÜNCELLE")
                    }
                },
                dismissButton = if (updateInfo!!.forceUpdate) null else {
                    {
                        TextButton(onClick = { showDialog = false }) {
                            Text("DAHA SONRA")
                        }
                    }
                }
            )
        }
    }

    private suspend fun fetchUpdateInfo(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val jsonStr = URL(UPDATE_URL).readText()
            val json = JSONObject(jsonStr)
            UpdateInfo(
                versionCode = json.getInt("versionCode"),
                versionName = json.getString("versionName"),
                downloadUrl = json.getString("downloadUrl"),
                forceUpdate = json.optBoolean("forceUpdate", false)
            )
        } catch (e: Exception) {
            null
        }
    }

    data class UpdateInfo(
        val versionCode: Int,
        val versionName: String,
        val downloadUrl: String,
        val forceUpdate: Boolean
    )
}
