package com.brainrush.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainrush.app.navigation.BrainRushNavGraph
import com.brainrush.app.presentation.BrainRushViewModel
import com.brainrush.app.ui.theme.BrainRushTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        val factory = BrainRushViewModel.Factory((application as BrainRushApplication).repository)
        setContent {
            val vm: BrainRushViewModel = viewModel(factory = factory)
            val settings by vm.appSettings.collectAsState()
            BrainRushTheme(themeMode = settings.themeMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BrainRushNavGraph(viewModel = vm, modifier = Modifier.fillMaxSize().padding(innerPadding))
                }
            }
        }
    }
}
