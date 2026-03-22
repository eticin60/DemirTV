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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL
import android.os.SystemClock
import com.onurcan.demirtv.R
import com.onurcan.demirtv.ui.theme.RedPrimary
import com.onurcan.demirtv.ui.theme.White
import com.onurcan.demirtv.ui.theme.DarkGrey
import com.onurcan.demirtv.ui.theme.SilverText
import androidx.compose.ui.graphics.Color

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
        var downloadedBytes by remember { mutableStateOf(0L) }
        var totalBytes by remember { mutableStateOf(0L) }
        var speedBps by remember { mutableStateOf(0L) }
        var avgSpeedBps by remember { mutableStateOf(0L) }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .blur(8.dp)
            )
            AlertDialog(
                onDismissRequest = {
                    if (!updateInfo!!.forceUpdate) showDialog = false
                },
                modifier = Modifier.border(1.dp, White.copy(alpha = 0.08f), RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                containerColor = DarkGrey,
                tonalElevation = 0.dp,
                titleContentColor = White,
                textContentColor = SilverText,
                title = { Text("Yeni Sürüm Hazır") },
                text = {
                    Column {
                        Image(
                            painter = painterResource(id = R.drawable.logo_demirtv),
                            contentDescription = "DemirTV",
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
                            Text("$downloadStatus %$progressPercent", color = RedPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            val sizeText = if (totalBytes > 0) {
                                "${formatMb(downloadedBytes)} / ${formatMb(totalBytes)}"
                            } else {
                                "İndirilen: ${formatMb(downloadedBytes)}"
                            }
                            Text("$sizeText • Hız: ${formatSpeed(speedBps)} • Ort: ${formatSpeed(avgSpeedBps)}", color = White)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        enabled = !isDownloading,
                        onClick = {
                            if (!context.packageManager.canRequestPackageInstalls()) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                                return@Button
                            }

                            isDownloading = true
                            downloadStatus = "İndiriliyor..."
                            progressPercent = 0
                            downloadedBytes = 0
                            totalBytes = 0
                            speedBps = 0
                            avgSpeedBps = 0
                            val info = updateInfo!!

                            // Start download & then trigger installer
                            scope.launch {
                                val result = downloadApk(
                                    context = context,
                                    url = info.downloadUrl,
                                    onProgress = { percent, downloaded, total, speed, avgSpeed ->
                                        progressPercent = percent
                                        downloadedBytes = downloaded
                                        totalBytes = total
                                        speedBps = speed
                                        avgSpeedBps = avgSpeed
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
                    , colors = ButtonDefaults.buttonColors(
                        containerColor = RedPrimary,
                        contentColor = White,
                        disabledContainerColor = RedPrimary.copy(alpha = 0.4f),
                        disabledContentColor = White.copy(alpha = 0.7f)
                    )
                    ) { Text(if (isDownloading) "İndiriliyor..." else "ŞİMDİ GÜNCELLE") }
                },
                dismissButton = if (updateInfo!!.forceUpdate) null else {
                    {
                        TextButton(onClick = { showDialog = false }) {
                            Text("DAHA SONRA", color = SilverText)
                        }
                    }
                }
            )
        }
    }

    private suspend fun fetchUpdateInfo(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val cacheBusted = "$UPDATE_URL?ts=${System.currentTimeMillis()}"
            val jsonStr = URL(cacheBusted).readText()
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
        onProgress: (Int, Long, Long, Long, Long) -> Unit
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

            var lastBytes = 0L
            var lastTime = SystemClock.elapsedRealtime()
            var avgSpeed = 0.0

            while (true) {
                val query = DownloadManager.Query().setFilterById(id)
                dm.query(query).use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        if (total > 0) {
                            val percent = ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                            val now = SystemClock.elapsedRealtime()
                            val deltaTimeMs = (now - lastTime).coerceAtLeast(1)
                            val deltaBytes = (downloaded - lastBytes).coerceAtLeast(0)
                            val speed = (deltaBytes * 1000L) / deltaTimeMs
                            avgSpeed = if (avgSpeed == 0.0) speed.toDouble() else (avgSpeed * 0.85 + speed * 0.15)
                            lastBytes = downloaded
                            lastTime = now
                            withContext(Dispatchers.Main) {
                                onProgress(percent, downloaded, total, speed, avgSpeed.toLong())
                            }
                        } else {
                            val now = SystemClock.elapsedRealtime()
                            val deltaTimeMs = (now - lastTime).coerceAtLeast(1)
                            val deltaBytes = (downloaded - lastBytes).coerceAtLeast(0)
                            val speed = (deltaBytes * 1000L) / deltaTimeMs
                            avgSpeed = if (avgSpeed == 0.0) speed.toDouble() else (avgSpeed * 0.85 + speed * 0.15)
                            lastBytes = downloaded
                            lastTime = now
                            withContext(Dispatchers.Main) {
                                onProgress(0, downloaded, total, speed, avgSpeed.toLong())
                            }
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

    private fun formatMb(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes / (1024.0 * 1024.0)
        return String.format("%.1f MB", mb)
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 MB/s"
        val mb = bytesPerSec / (1024.0 * 1024.0)
        return String.format("%.1f MB/s", mb)
    }
}
