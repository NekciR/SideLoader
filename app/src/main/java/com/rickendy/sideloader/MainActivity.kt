package com.rickendy.sideloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rickendy.sideloader.data.model.AppInfo
import com.rickendy.sideloader.ui.AppDetailScreen
import com.rickendy.sideloader.ui.AppListScreen
import com.rickendy.sideloader.ui.LoginScreen
import com.rickendy.sideloader.ui.theme.SideLoaderTheme
import com.rickendy.sideloader.viewmodel.AppViewModel
import com.rickendy.sideloader.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SideLoaderTheme(darkTheme = false) {
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val appViewModel: AppViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Track selected app for detail screen
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "app_list" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("app_list") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable("app_list") {
            AppListScreen(
                onAppClick = { app ->
                    selectedApp = app
                    navController.navigate("app_detail")
                },
                appViewModel = appViewModel
            )
        }

        composable("app_detail") {
            selectedApp?.let { app ->
                AppDetailScreen(
                    app = app,
                    onBackClick = { navController.popBackStack() },
                    onInstallClick = {  }
                )
            }
        }
    }
}