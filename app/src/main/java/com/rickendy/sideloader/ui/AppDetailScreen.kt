package com.rickendy.sideloader.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.AsyncImage
import com.rickendy.sideloader.data.model.AppInfo
import com.rickendy.sideloader.util.DownloadResult
import com.rickendy.sideloader.util.downloadAndInstall
import com.rickendy.sideloader.util.getInstalledVersionCode
import com.rickendy.sideloader.util.isAppInstalled
import com.rickendy.sideloader.util.openApp
import com.rickendy.sideloader.util.uninstallApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppInfo,
    onBackClick: () -> Unit,
    onInstallClick: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isInstalled by remember { mutableStateOf(isAppInstalled(context, app.id)) }
    val installedVersionCode = remember(isInstalled) {
        getInstalledVersionCode(context, app.id)
    }
    val hasUpdate = isInstalled && installedVersionCode < app.versionCode
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isInstalled = isAppInstalled(context, app.id)
        }
    }

    LaunchedEffect(app.id) {
        while (true) {
            isInstalled = isAppInstalled(context, app.id)
            kotlinx.coroutines.delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Card 1 — App identity
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = app.iconUrl,
                        contentDescription = "${app.name} icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "v${app.versionName} (${app.versionCode})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = app.id,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isInstalled) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Installed",
                                tint = if (hasUpdate)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (hasUpdate) "Update\nAvailable" else "Installed",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasUpdate)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Card 2 — Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = app.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (app.screenshots.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Screenshots",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(app.screenshots) { screenshotUrl ->
                                    AsyncImage(
                                        model = screenshotUrl,
                                        contentDescription = "Screenshot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .height(260.dp)
                                            .width(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    }


                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "What's New",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = app.changelog,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (isDownloading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LinearProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Downloading... $progress%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                if (isInstalled) {
                    Button(
                        onClick = { openApp(context, app.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open App")
                    }

                    if (hasUpdate) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isDownloading = true
                                    errorMessage = null
                                    val result = downloadAndInstall(
                                        context = context,
                                        apkUrl = app.apkUrl,
                                        appName = app.name,
                                        onProgress = { progress = it }
                                    )
                                    isDownloading = false
                                    when (result) {
                                        is DownloadResult.Success -> {
                                            isInstalled = isAppInstalled(context, app.id)
                                        }
                                        is DownloadResult.Error -> {
                                            errorMessage = result.message
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update to v${app.versionName}")
                        }
                    }


                    OutlinedButton(
                        onClick = {
                            uninstallApp(context, app.id)
                            isInstalled = isAppInstalled(context, app.id)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Uninstall")
                    }

                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                isDownloading = true
                                errorMessage = null
                                val result = downloadAndInstall(
                                    context = context,
                                    apkUrl = app.apkUrl,
                                    appName = app.name,
                                    onProgress = { progress = it }
                                )
                                isDownloading = false
                                when (result) {
                                    is DownloadResult.Success -> {
                                        isInstalled = isAppInstalled(context, app.id)
                                    }
                                    is DownloadResult.Error -> {
                                        errorMessage = result.message
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Install v${app.versionName}")
                    }

                    errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = message,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }



            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}