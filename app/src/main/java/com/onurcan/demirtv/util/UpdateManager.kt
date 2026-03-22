package com.onurcan.demirtv.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.onurcan.demirtv.BuildConfig
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

object UpdateManager {
    // GitHub Raw URL for update.json
    private const val UPDATE_URL = "https://raw.githubusercontent.com/eticin60/DemirTV/main/update.json"

    @Composable
    fun CheckForUpdates(context: Context) {
        var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
        var showDialog by remember { mutableStateOf(false) }
        var isDownloading by remember { mutableStateOf(false) }
        var downloadStatus by remember { mutableStateOf("İndiriliyor...") }
        var progressPercent by remember { mutableStateOf(0) }
        val scope = rememberCoroutineScope()
        
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
                text = {
                    Column {
                        Text(
                            if (updateInfo!!.forceUpdate) {
                                "Bu güncelleme zorunludur. Devam etmek için lütfen güncelleyin."
                            } else {
                                "DemirTV v${updateInfo!!.versionName} sürümü çıktı. Yeni özellikler ve performans iyileştirmeleri için güncelleyin."
                            }
                        )

                        if (isDownloading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(progress = progressPercent / 100f)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("$downloadStatus %$progressPercent")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !isDownloading,
                        onClick = {
                            if (!context.packageManager.canRequestPackageInstalls()) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                                return@TextButton
                            }

                            isDownloading = true
                            downloadStatus = "İndiriliyor..."
                            progressPercent = 0
                            val info = updateInfo!!

                            // Start download & then trigger installer
                            scope.launch {
                                val result = downloadApk(
                                    context = context,
                                    url = info.downloadUrl,
                                    onProgress = { percent ->
                                        progressPercent = percent
                                    }
                                )
                                if (result != null) {
                                    downloadStatus = "Kurulum başlatılıyor..."
                                    launchInstaller(context, result)
                                    if (!info.forceUpdate) showDialog = false
                                } else {
                                    downloadStatus = "İndirme başarısız oldu. Tekrar deneyin."
                                    isDownloading = false
                                }
                            }
                        }
                    ) { Text(if (isDownloading) "İndiriliyor..." else "ŞİMDİ GÜNCELLE") }
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

    private suspend fun downloadApk(
        context: Context,
        url: String,
        onProgress: (Int) -> Unit
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "DemirTV-update.apk"
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir != null && !downloadsDir.exists()) downloadsDir.mkdirs()
            val targetFile = File(downloadsDir, fileName)

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("DemirTV Güncelleme")
                .setDescription("Yeni sürüm indiriliyor")
                .setDestinationUri(Uri.fromFile(targetFile))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val id = dm.enqueue(request)

            while (true) {
                val query = DownloadManager.Query().setFilterById(id)
                dm.query(query).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        if (total > 0) {
                            val percent = ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                            onProgress(percent)
                        }
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            val apkUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                targetFile
                            )
                            return@withContext apkUri
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            return@withContext null
                        }
                    }
                }
                delay(400)
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun launchInstaller(context: Context, apkUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
