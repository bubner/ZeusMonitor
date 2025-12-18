package me.bubner.zeusmonitor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController

enum class Tab(val icon: ImageVector) {
    Monitor(Icons.Default.LocationOn),
    History(Icons.Default.History)
}

@Composable
fun Navbar(navController: NavController) {
    var selectedDestination by rememberSaveable { mutableIntStateOf(0) }

    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        Tab.entries.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedDestination == index,
                onClick = {
                    navController.navigate(route = tab.name) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    selectedDestination = index
                },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = tab.name
                    )
                },
                label = { Text(tab.name) }
            )
        }
    }
}