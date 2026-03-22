package com.onurcan.demirtv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.onurcan.demirtv.ui.screens.*
import com.onurcan.demirtv.ui.theme.DemirTVTheme
import com.onurcan.demirtv.ui.viewmodel.DashboardViewModel
import com.onurcan.demirtv.util.UpdateManager

import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_DemirTV)
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        
        setContent {
            DemirTVTheme {
                UpdateManager.CheckForUpdates(context = LocalContext.current)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: DashboardViewModel = viewModel()
    var currentChannel by remember { mutableStateOf<com.onurcan.demirtv.data.model.Channel?>(null) }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onSplashFinished = {
                navController.navigate("profiles") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        
        composable("profiles") {
            ProfileScreen(onProfileSelected = { profile ->
                viewModel.selectProfile(profile)
                navController.navigate("dashboard") {
                    popUpTo("profiles") { inclusive = true }
                }
            })
        }
        
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onChannelSelected = { channel ->
                    currentChannel = channel
                    navController.navigate("player")
                }
            )
        }
        
        composable("player") {
            currentChannel?.let { channel ->
                PlayerScreen(channel = channel)
            }
        }
    }
}
