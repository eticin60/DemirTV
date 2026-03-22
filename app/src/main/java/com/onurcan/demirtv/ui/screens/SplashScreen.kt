package com.onurcan.demirtv.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onurcan.demirtv.R
import com.onurcan.demirtv.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isTV = configuration.screenWidthDp >= 900
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
        label = "alpha"
    )
    
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) (if (isTV) 1.2f else 1.05f) else 0.8f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2800)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F0F0F), Color(0xFF1E1E1E), Color(0xFF0F0F0F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative Blurry "Liquid" Background Elements
        Box(
            modifier = Modifier
                .offset(x = (if (isTV) (-100).dp else (-50).dp), y = (if (isTV) (-150).dp else (-80).dp))
                .size(if (isTV) 300.dp else 200.dp)
                .clip(CircleShape)
                .background(RedPrimary.copy(alpha = 0.1f))
                .blur(if (isTV) 80.dp else 40.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
                .padding(32.dp)
        ) {
            // Unrestricted Logo
            Image(
                painter = painterResource(id = R.drawable.logo_demirtv),
                contentDescription = "DemirTV Logo",
                modifier = Modifier
                    .size(if (isTV) 220.dp else 160.dp)
                    .clip(RoundedCornerShape(40.dp))
            )
            
            Spacer(modifier = Modifier.height(if (isTV) 48.dp else 32.dp))
            
            Text(
                text = "DEMİRTV",
                color = White,
                fontSize = if (isTV) 54.sp else 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = if (isTV) 12.sp else 8.sp,
                modifier = Modifier.blur(if (startAnimation) 0.dp else 8.dp)
            )
            
            Text(
                text = "PREMIUM CANLI YAYIN",
                color = RedPrimary,
                fontSize = if (isTV) 16.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = if (isTV) 8.sp else 4.sp,
                modifier = Modifier.alpha(0.9f)
            )
        }
    }
}
