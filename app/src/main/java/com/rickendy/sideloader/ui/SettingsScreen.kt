package com.rickendy.sideloader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rickendy.sideloader.data.model.User
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.Alignment
import com.rickendy.sideloader.ui.shared.SectionHeader
import com.rickendy.sideloader.ui.shared.TransparentCard
import com.rickendy.sideloader.ui.shared.TransparentTopAppBar
import com.rickendy.sideloader.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: User,
    themeViewModel: ThemeViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val appVersion = packageInfo.versionName

    var showLogoutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val theme by themeViewModel.theme.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TransparentTopAppBar(title = "Settings")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransparentCard (contentPadding = PaddingValues(16.dp))  {
                SectionHeader(title = "Account")
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Text(
                    text = currentUser.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = currentUser.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TransparentCard (contentPadding = PaddingValues(16.dp)) {
                SectionHeader(title = "About")
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = appVersion ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TransparentCard(contentPadding = PaddingValues(16.dp)) {
                SectionHeader(title = "Appearance")
                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeChip(
                            label = "System",
                            selected = theme == "system",
                            onClick = { themeViewModel.setTheme("system") }
                        )
                        ThemeChip(
                            label = "Light",
                            selected = theme == "light",
                            onClick = { themeViewModel.setTheme("light") }
                        )
                        ThemeChip(
                            label = "Dark",
                            selected = theme == "dark",
                            onClick = { themeViewModel.setTheme("dark") }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { showLogoutSheet = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Text("Logout")
            }
        }

        if (showLogoutSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLogoutSheet = false },
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Are you sure you want to logout?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showLogoutSheet = false
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Yes, Logout")
                    }

                    OutlinedButton(
                        onClick = { showLogoutSheet = false },
                        modifier = Modifier.fillMaxWidth(),

                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    )
}