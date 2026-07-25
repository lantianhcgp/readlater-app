package com.lantianhcgp.readlater

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lantianhcgp.readlater.ui.navigation.AppNavigation
import com.lantianhcgp.readlater.ui.theme.ReadLaterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadLaterTheme {
                AppNavigation()
            }
        }
    }
}
