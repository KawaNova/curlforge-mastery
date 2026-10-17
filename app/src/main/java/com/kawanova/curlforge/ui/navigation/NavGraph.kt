package com.kawanova.curlforge.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kawanova.curlforge.ui.*
import com.kawanova.curlforge.ui.theme.CurlCyan
import com.kawanova.curlforge.ui.theme.Slate400
import com.kawanova.curlforge.ui.theme.Slate900

sealed class Screen(val route: String, val titleJa: String, val titleEn: String, val icon: ImageVector) {
    object Converter : Screen("converter", "変換", "Code", Icons.Default.Terminal)
    object Presets : Screen("presets", "プリセット", "Presets", Icons.Default.AutoAwesome)
    object Glossary : Screen("glossary", "辞典", "Glossary", Icons.AutoMirrored.Filled.MenuBook)
    object Saved : Screen("saved", "保存済", "Saved", Icons.Default.Bookmark)
}

val bottomNavItems = listOf(
    Screen.Converter,
    Screen.Presets,
    Screen.Glossary,
    Screen.Saved
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: CurlViewModel,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Slate900
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.titleJa) },
                        label = { Text(screen.titleJa) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CurlCyan,
                            selectedTextColor = CurlCyan,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400,
                            indicatorColor = Slate900
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Converter.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Converter.route) {
                CurlConverterScreen(
                    viewModel = viewModel,
                    onNavigateToPresets = {
                        navController.navigate(Screen.Presets.route)
                    },
                    onNavigateToGlossary = {
                        navController.navigate(Screen.Glossary.route)
                    }
                )
            }
            composable(Screen.Presets.route) {
                CurlPresetsScreen(
                    viewModel = viewModel,
                    onPresetLoaded = {
                        navController.navigate(Screen.Converter.route) {
                            popUpTo(Screen.Converter.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Glossary.route) {
                CurlGlossaryScreen(
                    viewModel = viewModel
                )
            }
            composable(Screen.Saved.route) {
                SavedCurlScreen(
                    viewModel = viewModel,
                    onSnippetLoaded = {
                        navController.navigate(Screen.Converter.route) {
                            popUpTo(Screen.Converter.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
