package com.onurcan.demirtv.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurcan.demirtv.data.model.Profile
import com.onurcan.demirtv.data.repository.ProfileRepository
import com.onurcan.demirtv.ui.theme.*

@Composable
fun ProfileScreen(onProfileSelected: (Profile) -> Unit) {
    val profiles = ProfileRepository.getProfiles()
    val configuration = LocalConfiguration.current
    val isTV = configuration.screenWidthDp >= 900
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2B2B2B), Color(0xFF000000)),
                    radius = 1500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // High-end glass background glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, RedPrimary.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
                .blur(80.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp)
        ) {
            Text(
                text = "Kim izliyor?",
                color = White,
                fontSize = if (isTV) 56.sp else 38.sp,
                fontWeight = FontWeight.ExtraLight,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(bottom = 80.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(minOf(profiles.size, if (isTV) 5 else 3)),
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalArrangement = Arrangement.spacedBy(60.dp),
                modifier = Modifier.wrapContentWidth()
            ) {
                items(profiles) { profile ->
                    ProfileCard(profile = profile, isTV = isTV, onSelected = { onProfileSelected(profile) })
                }
            }
        }
    }
}

@Composable
fun ProfileCard(profile: Profile, isTV: Boolean, onSelected: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val cardSize = if (isTV) 200.dp else 140.dp
    
    val profileColor = when(profile.name) {
        "Akın" -> ProfileBlue
        "Zeynep" -> ProfilePurple
        "Ayşenur" -> ProfileOrange
        "Onurcan" -> ProfileGreen
        "Esmanur" -> ProfileKids
        else -> RedPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(cardSize)
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onSelected() }
            .focusable()
    ) {
        // High-Definition Liquid Glass Profile Icon
        Box(
            modifier = Modifier
                .size(cardSize)
                .clip(RoundedCornerShape(32.dp))
                .background(profileColor.copy(alpha = 0.25f))
                .border(
                    width = if (isFocused) 5.dp else 2.dp,
                    color = if (isFocused) White else White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(32.dp)
                )
                .shadow(
                    elevation = if (isFocused) 40.dp else 0.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = profileColor,
                    ambientColor = profileColor
                ),
            contentAlignment = Alignment.Center
        ) {
            // Internal Glass Reflections
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(White.copy(alpha = 0.15f), Color.Transparent, White.copy(alpha = 0.05f))
                        )
                    )
            )
            
            // Premium Avatar Image
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.onurcan.demirtv.R.drawable.avatar),
                contentDescription = "Profile Avatar",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(32.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = profile.name,
            color = if (isFocused) White else SilverText,
            fontSize = if (isTV) 28.sp else 18.sp,
            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Light,
            letterSpacing = 1.sp
        )
    }
}
