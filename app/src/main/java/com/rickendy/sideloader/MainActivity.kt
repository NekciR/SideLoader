package com.rickendy.sideloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rickendy.sideloader.data.model.AppInfo
import com.rickendy.sideloader.ui.AppDetailScreen
import com.rickendy.sideloader.ui.AppListScreen
import com.rickendy.sideloader.ui.LoginScreen
import com.rickendy.sideloader.ui.SettingsScreen
import com.rickendy.sideloader.ui.shared.AnimatedBackground
import com.rickendy.sideloader.ui.theme.SideLoaderTheme
import com.rickendy.sideloader.viewmodel.AppViewModel
import com.rickendy.sideloader.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SideLoaderTheme (darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val appViewModel: AppViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSessionRestored by authViewModel.isSessionRestored.collectAsState()
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

    var backgroundSpeed by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    var installCompleted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ){

        AnimatedBackground(
            enableParallax = true,
            speedMultiplier = backgroundSpeed,
            patchingCompleted = installCompleted
        )

        if (!isSessionRestored) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SideLoader",
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            return
        }

        if (currentUser == null) {
            LoginScreen(
                onLoginSuccess = {},
                authViewModel = authViewModel
            )
            return
        }

        val bottomNavController = rememberNavController()
        val currentBackStack by bottomNavController.currentBackStackEntryAsState()
        val currentRoute = currentBackStack?.destination?.route

        val bottomBarRoutes = listOf("app_list", "settings")

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentRoute in bottomBarRoutes) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == "app_list",
                            onClick = {
                                bottomNavController.navigate("app_list") {
                                    popUpTo("app_list") { inclusive = true }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Apps"
                                )
                            },
                            label = { Text("Apps") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "settings",
                            onClick = {
                                bottomNavController.navigate("settings") {
                                    popUpTo("app_list")
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings") }
                        )
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = bottomNavController,
                startDestination = "app_list",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("app_list") {
                    AppListScreen(
                        onAppClick = { app ->
                            selectedApp = app
                            bottomNavController.navigate("app_detail")
                        },
                        appViewModel = appViewModel
                    )
                }

                composable("app_detail") {
                    selectedApp?.let { app ->
                        AppDetailScreen(
                            app = app,
                            onSpeedChange = {speed -> backgroundSpeed = speed},
                            onBackClick = {
                                bottomNavController.popBackStack()
                                backgroundSpeed = 1f
                                          },
                            onInstallClick = { }
                        )
                    }
                }

                composable("settings") {
                    SettingsScreen(
                        currentUser = currentUser!!,
                        onLogout = { authViewModel.logout() }
                    )
                }
            }
        }
    }


}