package com.onurcan.demirtv.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.onurcan.demirtv.data.model.Channel
import com.onurcan.demirtv.ui.theme.*
import com.onurcan.demirtv.ui.viewmodel.DashboardViewModel

import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onChannelSelected: (Channel) -> Unit
) {
    val channels by viewModel.filteredChannels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp >= 900

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF252525), Color(0xFF000000)),
                    radius = 2000f
                )
            )
    ) {
        // High-end ambient glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, RedPrimary.copy(alpha = 0.05f), Color.Transparent)
                    )
                )
                .blur(100.dp)
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Liquid Glass Header
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    color = White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "DEMİRTV",
                            color = RedPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "CANLI YAYIN",
                            color = White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        ) { padding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RedPrimary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = if (isTV) 220.dp else 160.dp),
                    contentPadding = PaddingValues(
                        start = 24.dp, 
                        end = 24.dp, 
                        top = padding.calculateTopPadding() + 16.dp, 
                        bottom = 32.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(channels) { channel ->
                        LiquidChannelCard(channel = channel, onClick = { onChannelSelected(channel) })
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidChannelCard(channel: Channel, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.1f else 1f, label = "scale")
    
    Column(
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onClick() }
            .focusable()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // High-Definition Glass Container
        Box(
            modifier = Modifier
                .aspectRatio(16/9f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(White.copy(alpha = if (isFocused) 0.15f else 0.05f))
                .border(
                    width = if (isFocused) 3.dp else 1.dp,
                    color = if (isFocused) White else White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .shadow(
                    elevation = if (isFocused) 30.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = RedPrimary.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glass reflections
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(White.copy(alpha = 0.1f), Color.Transparent, White.copy(alpha = 0.05f))
                        )
                    )
            )

            if (channel.logoUrl != null) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isFocused) RedPrimary else White.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = channel.name,
            color = if (isFocused) White else SilverText,
            fontSize = 16.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            letterSpacing = 1.sp
        )
    }
}
