package com.example.packratio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.packratio.ui.navigation.PackRatioNavHost
import com.example.packratio.ui.theme.PackRatioTheme
import com.example.packratio.viewmodel.PackRatioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PackRatioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            PackRatioTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PackRatioNavHost(viewModel = viewModel)
                }
            }
        }
    }
}