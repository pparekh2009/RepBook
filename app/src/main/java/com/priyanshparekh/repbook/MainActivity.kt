package com.priyanshparekh.repbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.priyanshparekh.repbook.data.preferences.AppPreferences
import com.priyanshparekh.repbook.navigation.RepBookBottomNavigation
import com.priyanshparekh.repbook.navigation.RepBookNavHost
import com.priyanshparekh.repbook.ui.theme.RepBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RepBookApplication).container
        setContent {
            val preferences by container.preferencesDataStore.appPreferencesFlow
                .collectAsState(initial = AppPreferences())

            RepBookTheme(themeMode = preferences.themeMode) {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { RepBookBottomNavigation(navController) }
                ) { innerPadding ->
                    RepBookNavHost(
                        navController = navController,
                        container = container,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
