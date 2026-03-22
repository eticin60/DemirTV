package com.onurcan.demirtv.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.onurcan.demirtv.data.model.Channel
import com.onurcan.demirtv.ui.theme.*
import com.onurcan.demirtv.util.WatchStats

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(channel: Channel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    LaunchedEffect(channel.streamUrl) {
        WatchStats.incrementWatch(context, channel)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.hls.HlsMediaSource.Factory(
                    androidx.media3.datasource.DefaultHttpDataSource.Factory()
                        .setUserAgent("DemirTV/1.0")
                )
            )
            .build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(channel.streamUrl)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
    }

    var showQualityMenu by remember { mutableStateOf(false) }

    DisposableEffect(channel, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_STOP -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.seekToDefaultPosition()
                    exoPlayer.playWhenReady = true
                }
                Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(BlackBackground)
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    requestFocus()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Glassmorphic Quality Button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(White.copy(alpha = 0.1f))
                .border(1.dp, White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .clickable { showQualityMenu = !showQualityMenu }
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(text = "Kalite", color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        if (showQualityMenu) {
            QualitySelectionMenu(
                onQualitySelected = { maxHeight ->
                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                        .buildUpon()
                        .setMaxVideoSize(maxHeight * 16 / 9, maxHeight)
                        .build()
                    showQualityMenu = false
                },
                onClose = { showQualityMenu = false }
            )
        }
    }
}

@Composable
fun QualitySelectionMenu(onQualitySelected: (Int) -> Unit, onClose: () -> Unit) {
    val options = listOf(
        "Auto" to Int.MAX_VALUE,
        "4K (2160p)" to 2160,
        "Full HD (1080p)" to 1080,
        "HD (720p)" to 720,
        "SD (480p)" to 480
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DarkGrey.copy(alpha = 0.9f))
                .border(1.dp, White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("İzleme Kalitesi", color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            options.forEach { (label, height) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onQualitySelected(height) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = SilverText,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
