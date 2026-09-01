package com.ambesoftnet.cleanvault.presentation.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ArchitectureHighlight(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(
    onMenuClick: () -> Unit
) {
    val highlights = listOf(
        ArchitectureHighlight(
            title = "Clean Architecture",
            description = "Strict separation into Data (Room & Retrofit), Domain (UseCases & Models), and Presentation (Compose & ViewModel) layers with zero circular dependencies.",
            icon = Icons.Default.Architecture,
            tag = "Architecture"
        ),
        ArchitectureHighlight(
            title = "Jetpack Compose & Material 3",
            description = "100% declarative UI built with Material You (Material 3), dynamic theming, smooth animations, and zero legacy XML layouts.",
            icon = Icons.Default.Widgets,
            tag = "UI Framework"
        ),
        ArchitectureHighlight(
            title = "MVVM & Unidirectional Data Flow",
            description = "State-driven ViewModels exposing immutable StateFlow UI states. User actions trigger UseCases and auto-recompose screens.",
            icon = Icons.Default.Code,
            tag = "Pattern"
        ),
        ArchitectureHighlight(
            title = "Android KeyStore & AES-256",
            description = "Hardware-backed AES-256-GCM encryption using EncryptedSharedPreferences and KeyStore master keys. Zero plaintext secrets on disk.",
            icon = Icons.Default.Lock,
            tag = "Security"
        ),
        ArchitectureHighlight(
            title = "Room Database & SQLite",
            description = "Offline-first reactive persistence with Room DAO, automatic Flow query emissions, and encrypted ciphertext storage.",
            icon = Icons.Default.Storage,
            tag = "Persistence"
        ),
        ArchitectureHighlight(
            title = "Kotlin Coroutines & Flow",
            description = "Non-blocking background I/O execution with Dispatchers.IO, cold Flows, and lifecycle-aware StateFlow subscriptions.",
            icon = Icons.Default.Sync,
            tag = "Async"
        ),
        ArchitectureHighlight(
            title = "Dagger Hilt Dependency Injection",
            description = "Compile-time dependency graph generation for singletons, repositories, DAOs, and ViewModel constructor injection.",
            icon = Icons.Default.Extension,
            tag = "DI"
        ),
        ArchitectureHighlight(
            title = "Retrofit 2 & Gson REST Client",
            description = "Type-safe HTTP REST client integration configured with suspend functions and background thread dispatchers.",
            icon = Icons.Default.CheckCircle,
            tag = "Networking"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Clean Vault") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔐 CleanVault Architecture Showcase",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "This production-grade Android application demonstrates modern, scalable Android engineering practices and architectural standards.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            items(highlights) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        FilledTonalIconButton(
                            onClick = { },
                            modifier = Modifier.size(44.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(item.tag, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
