package com.rickendy.sideloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.unit.dp
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
import com.rickendy.sideloader.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val appViewModel: AppViewModel = viewModel()
    val themeViewModel: ThemeViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isSessionRestored by authViewModel.isSessionRestored.collectAsState()
    val theme by themeViewModel.theme.collectAsState()
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var backgroundSpeed by remember { mutableFloatStateOf(1f) }
    var installCompleted by remember { mutableStateOf(false) }

    val darkTheme = when (theme) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    SideLoaderTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
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
                    return@Surface
                }

                if (currentUser == null) {
                    LoginScreen(
                        onLoginSuccess = {},
                        authViewModel = authViewModel
                    )
                    return@Surface
                }

                val bottomNavController = rememberNavController()
                val currentBackStack by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route
                val bottomBarRoutes = listOf("app_list", "settings")

                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (currentRoute in bottomBarRoutes) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 3.dp
                            ) {
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
                                    label = { Text("Apps") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                                    label = { Text("Settings") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                            val accessToken by authViewModel.accessToken.collectAsState()
                            LaunchedEffect(accessToken) {
                                accessToken?.let { appViewModel.loadApps(it) }
                            }
                            AppListScreen(
                                onAppClick = { app ->
                                    selectedApp = app
                                    bottomNavController.navigate("app_detail")
                                },
                                authViewModel = authViewModel,
                                appViewModel = appViewModel
                            )
                        }

                        composable("app_detail") {
                            selectedApp?.let { app ->
                                AppDetailScreen(
                                    app = app,
                                    appViewModel = appViewModel,
                                    onBackClick = { bottomNavController.popBackStack() },
                                    onSpeedChange = { speed -> backgroundSpeed = speed }
                                )
                            }
                        }

                        composable("settings") {
                            SettingsScreen(
                                currentUser = currentUser!!,
                                themeViewModel = themeViewModel,
                                onLogout = { authViewModel.logout() }
                            )
                        }
                    }
                }
            }
        }
    }
}