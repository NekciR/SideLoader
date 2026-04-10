package com.rickendy.sideloader.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.HorizontalDivider
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
import com.rickendy.sideloader.ui.shared.SectionHeader
import com.rickendy.sideloader.ui.shared.TransparentCard
import com.rickendy.sideloader.ui.shared.TransparentTopAppBar
import com.rickendy.sideloader.util.DownloadResult
import com.rickendy.sideloader.util.downloadAndInstall
import com.rickendy.sideloader.util.getInstalledVersionCode
import com.rickendy.sideloader.util.isAppInstalled
import com.rickendy.sideloader.util.openApp
import com.rickendy.sideloader.util.uninstallApp
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkInfo
import com.rickendy.sideloader.util.DownloadManager
import com.rickendy.sideloader.util.formatDate
import com.rickendy.sideloader.viewmodel.AppViewModel
import com.rickendy.sideloader.worker.DownloadWorker
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppInfo,
    appViewModel: AppViewModel = viewModel(),
    onBackClick: () -> Unit,
    onSpeedChange: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val activeDownloads by appViewModel.activeDownloads.collectAsState()
    val workId = activeDownloads[app.packageName]
    val workInfo = workId?.let {
        DownloadManager.getWorkInfo(context, it).observeAsState()
    }

    val isDownloading = workId != null && workInfo?.value?.state != WorkInfo.State.SUCCEEDED
            && workInfo?.value?.state != WorkInfo.State.FAILED
            && workInfo?.value?.state != WorkInfo.State.CANCELLED
    val progress = workInfo?.value?.progress?.getInt(
        DownloadWorker.KEY_PROGRESS, 0
    ) ?: 0
    val downloadFailed = workInfo?.value?.state == WorkInfo.State.FAILED
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isInstalled by remember { mutableStateOf(isAppInstalled(context, app.packageName)) }
    val installedVersionCode = remember(isInstalled) {
        getInstalledVersionCode(context, app.packageName)
    }
    val hasUpdate = isInstalled && installedVersionCode < app.versionCode
    val lifecycleOwner = LocalLifecycleOwner.current

    var showUninstallSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isInstalled = isAppInstalled(context, app.packageName)
        }
    }

    LaunchedEffect(app.packageName) {
        while (true) {
            isInstalled = isAppInstalled(context, app.packageName)
            kotlinx.coroutines.delay(1000)
        }
    }

    LaunchedEffect(workInfo?.value?.state) {
        when (workInfo?.value?.state) {
            WorkInfo.State.RUNNING -> {
                onSpeedChange(10f)
            }
            WorkInfo.State.SUCCEEDED -> {
                onSpeedChange(1f)
                appViewModel.clearDownload(app.packageName)
            }
            WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                onSpeedChange(1f)
                appViewModel.clearDownload(app.packageName)
            }
            else -> {}
        }
    }

    BackHandler {
        onSpeedChange(1f)
        onBackClick()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TransparentTopAppBar(
                title = app.name,
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
            TransparentCard {
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
                            text = app.packageName,
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
            TransparentCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SectionHeader(title = "Description")
                    Text(
                        text = app.description,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (app.screenshots.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                            SectionHeader(title = "Screenshots")
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

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "What's New")
                        app.changelogs.forEach { changelog ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "v${changelog.versionName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatDate(changelog.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = changelog.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (changelog != app.changelogs.last()) {
                                Divider()
                            }
                        }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Downloading... $progress%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                appViewModel.cancelDownload(context, app.packageName)
                                onSpeedChange(1f)
                            }
                        ) {
                            Text(
                                text = "Cancel",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            } else {
                if (isInstalled) {
                    Button(
                        onClick = { openApp(context, app.packageName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open App")
                    }

                    if (hasUpdate) {
                        Button(
                            onClick = {
                                appViewModel.startDownload(context, app)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Install v${app.versionName}")
                        }
                    }


                    OutlinedButton(
                        onClick = { showUninstallSheet = true },
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
                            appViewModel.startDownload(context, app)
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

        if (showUninstallSheet) {
            ModalBottomSheet(
                onDismissRequest = { showUninstallSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Uninstall ${app.name}?",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "This will remove the app and all its data from your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showUninstallSheet = false
                            uninstallApp(context, app.packageName)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Yes, Uninstall")
                    }

                    OutlinedButton(
                        onClick = { showUninstallSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}