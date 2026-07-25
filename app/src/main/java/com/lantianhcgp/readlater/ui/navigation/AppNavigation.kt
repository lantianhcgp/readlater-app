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
import com.lantianhcgp.readlater.ui.addlink.AddLinkScreen
import com.lantianhcgp.readlater.ui.favorites.FavoritesScreen
import com.lantianhcgp.readlater.ui.inbox.InboxScreen
import com.lantianhcgp.readlater.ui.reader.ReaderScreen
import com.lantianhcgp.readlater.ui.settings.SettingsScreen
import com.lantianhcgp.readlater.ui.tags.TagArticlesScreen
import com.lantianhcgp.readlater.ui.tags.TagsScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Inbox : Screen("inbox", "收件箱", Icons.Default.Home)
    data object Tags : Screen("tags", "标签", Icons.Default.Label)
    data object Favorites : Screen("favorites", "收藏夹", Icons.Default.Favorite)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}

val bottomNavItems = listOf(Screen.Inbox, Screen.Tags, Screen.Favorites, Screen.Settings)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                    onArticleClick = { navController.navigate("reader/$it") },
                    onAddClick = { navController.navigate("addLink") }
                )
            }
            composable(Screen.Tags.route) {
                TagsScreen(
                    onTagClick = { tagId, tagName ->
                        navController.navigate("tagArticles/$tagId/$tagName")
                    }
                )
            }
            composable(Screen.Favorites.route) { FavoritesScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                route = "reader/{articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.StringType })
            ) {
                ReaderScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = "tagArticles/{tagId}/{tagName}",
                arguments = listOf(
                    navArgument("tagId") { type = NavType.StringType },
                    navArgument("tagName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
                val tagName = backStackEntry.arguments?.getString("tagName") ?: ""
                TagArticlesScreen(
                    tagName = tagName,
                    onBack = { navController.popBackStack() },
                    onArticleClick = { navController.navigate("reader/$it") }
                )
            }
            composable("addLink") {
                AddLinkScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
