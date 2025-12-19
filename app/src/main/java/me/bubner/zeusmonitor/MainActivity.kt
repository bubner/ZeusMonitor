package me.bubner.zeusmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import me.bubner.zeusmonitor.timer.HistoryDataStore
import me.bubner.zeusmonitor.ui.DeleteAllHistoryButton
import me.bubner.zeusmonitor.ui.theme.ZeusMonitorTheme

/**
 * Zeus Monitor
 *
 * @author Lucas Bubner, 2025
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Main() }
    }
}

@Preview
@Composable
fun Main() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val historyStore = remember(context) { HistoryDataStore(context) }

    // We prefer to use our own colours rather than the user's (yellow/blue)
    ZeusMonitorTheme(dynamicColor = false) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { Navbar(navController) },
            floatingActionButton = {
                // FAB for History tab
                AnimatedVisibility(
                    visible = currentRoute == Tab.History.name,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    DeleteAllHistoryButton {
                        coroutineScope.launch { historyStore.deleteAll() }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Tab.Monitor.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                Tab.entries.forEach { tab ->
                    composable(tab.name) {
                        when (tab) {
                            Tab.Monitor -> MainScreen(onNewItem = {
                                coroutineScope.launch {
                                    historyStore.pushHistoryItem(it)
                                }
                            })

                            Tab.History -> HistoryScreen(historyStore.historyFlow())
                        }
                    }
                }
            }
        }

    }
}