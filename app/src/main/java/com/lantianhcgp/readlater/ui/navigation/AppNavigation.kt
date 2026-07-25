package com.lantianhcgp.readlater.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lantianhcgp.readlater.ui.favorites.FavoritesScreen
import com.lantianhcgp.readlater.ui.inbox.InboxScreen
import com.lantianhcgp.readlater.ui.reader.ReaderScreen
import com.lantianhcgp.readlater.ui.settings.SettingsScreen
import com.lantianhcgp.readlater.ui.tags.TagsScreen

sealed class Screen(val route: String) {
    data object Inbox : Screen("inbox")
    data object Tags : Screen("tags")
    data object Favorites : Screen("favorites")
    data object Settings : Screen("settings")
    data object Reader : Screen("reader/{articleId}") {
        fun createRoute(articleId: String) = "reader/$articleId"
    }
    data object AddLink : Screen("add_link")
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("收件箱", Icons.Default.Home, Screen.Inbox),
    BottomNavItem("标签", Icons.Default.Label, Screen.Tags),
    BottomNavItem("收藏夹", Icons.Default.Favorite, Screen.Favorites),
    BottomNavItem("设置", Icons.Default.Settings, Screen.Settings),
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == item.screen.route
                        } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inbox.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Inbox.route) {
                InboxScreen(
                    onArticleClick = { articleId ->
                        navController.navigate(Screen.Reader.createRoute(articleId))
                    }
                )
            }
            composable(Screen.Tags.route) {
                TagsScreen()
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onArticleClick = { articleId ->
                        navController.navigate(Screen.Reader.createRoute(articleId))
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(
                route = Screen.Reader.route,
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
                ReaderScreen(articleId = articleId)
            }
        }
    }
}
