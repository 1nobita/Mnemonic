package com.example.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.viewmodel.AuraViewModel
import com.example.ui.viewmodel.FutureTwinViewModel
import com.example.ui.viewmodel.MnemViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Aura : Screen("aura", "Aura", Icons.Filled.Home)
    object Mnems : Screen("mnems", "Mnems", Icons.Filled.List)
    object FutureTwin : Screen("future_twin", "FutureTwin", Icons.Filled.Star)
}

val items = listOf(
    Screen.Aura,
    Screen.Mnems,
    Screen.FutureTwin
)

@Composable
fun AppNavigation(
    auraViewModel: AuraViewModel,
    mnemViewModel: MnemViewModel,
    futureTwinViewModel: FutureTwinViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Aura.route, Modifier.padding(innerPadding)) {
            composable(Screen.Aura.route) { AuraScreen(auraViewModel) }
            composable(Screen.Mnems.route) { MnemScreen(mnemViewModel) }
            composable(Screen.FutureTwin.route) { FutureTwinScreen(futureTwinViewModel) }
        }
    }
}
