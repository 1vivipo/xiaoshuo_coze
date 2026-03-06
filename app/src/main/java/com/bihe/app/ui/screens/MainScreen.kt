package com.bihe.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    label = { Text("创作") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Movie, contentDescription = null) },
                    label = { Text("漫剧") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                    label = { Text("推文") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                    label = { Text("模型") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("我的") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> CreationScreen(
                    onNavigateToEditor = { projectId -> 
                        navController.navigate("editor/$projectId")
                    }
                )
                1 -> DramaScreen()
                2 -> PromoScreen()
                3 -> ModelScreen()
                4 -> SettingsScreen()
            }
        }
    }
}

    NavHost(
        navController = navController,
        startDestination = "creation",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("creation") {
            CreationScreen(
                onNavigateToEditor = { projectId -> 
                    navController.navigate("editor/$projectId")
                }
            )
        }
        composable("editor/{projectId}") { backStackEntry ->
            val projectId = backStackEntry?.arguments?.get("projectId")?.toString()?.toLongOrNull ?: 0L
            EditorScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
