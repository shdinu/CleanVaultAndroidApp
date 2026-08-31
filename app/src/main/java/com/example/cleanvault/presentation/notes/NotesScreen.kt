package com.example.cleanvault.presentation.notes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cleanvault.domain.model.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var secretKeyInput by remember { mutableStateOf("") }
    var secretValInput by remember { mutableStateOf("") }
    var showSecretSection by remember { mutableStateOf(false) }

    // Dialog state for adding or editing a note
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }

    // Delete confirmation dialog
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clean Vault") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Open Drawer Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showSecretSection = !showSecretSection }) {
                        Icon(
                            imageVector = if (showSecretSection) Icons.Default.VpnKeyOff else Icons.Default.VpnKey,
                            contentDescription = "Toggle KeyStore Secret Card"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingNote = null
                    noteTitleInput = ""
                    noteContentInput = ""
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Note")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxSize()
        ) {
            // ─── SEARCH BAR ──────────────────────────────────────────────────
            if (uiState is NotesUiState.Success) {
                val successState = uiState as NotesUiState.Success
                OutlinedTextField(
                    value = successState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search notes by title or content...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (successState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ─── OPTIONAL: KeyStore Secret Input Card ────────────────────────
            AnimatedVisibility(visible = showSecretSection) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "KeyStore Raw Secret Manager",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = secretKeyInput,
                            onValueChange = { secretKeyInput = it },
                            label = { Text("Key Identifier (e.g. master_pin)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = secretValInput,
                            onValueChange = { secretValInput = it },
                            label = { Text("Secret Value to Encrypt") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = {
                                    val key = secretKeyInput.ifBlank { "user_vault_key" }
                                    if (secretValInput.isNotBlank()) {
                                        viewModel.onSaveSecret(key, secretValInput)
                                        secretValInput = ""
                                        Toast.makeText(context, "Secret encrypted in Android KeyStore", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Encrypt & Save")
                            }
                        }
                    }
                }
            }

            // ─── MAIN CONTENT: State-Driven List ────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (val state = uiState) {
                    is NotesUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is NotesUiState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is NotesUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Display decrypted single secret if present
                            state.decryptedSecret?.let { secret ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "KeyStore Decrypted: $secret",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { copyToClipboard(secret, "Secret") }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Secret", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            if (state.notes.isEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .padding(32.dp)
                                            .fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.NoteAlt,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = if (state.searchQuery.isNotBlank()) "No matching notes found" else "No Notes in Vault",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (state.searchQuery.isNotBlank()) "Try searching for a different keyword" else "Tap the '+' button below to create your first encrypted vault note.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(state.notes, key = { it.id }) { note ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                // Header: Title & Actions
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = note.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )

                                                    Row {
                                                        IconButton(
                                                            onClick = { copyToClipboard(note.decryptedContent, "Note content") },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Content", modifier = Modifier.size(18.dp))
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                editingNote = note
                                                                noteTitleInput = note.title
                                                                noteContentInput = note.decryptedContent
                                                                showAddEditDialog = true
                                                            },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Edit Note", modifier = Modifier.size(18.dp))
                                                        }
                                                        IconButton(
                                                            onClick = { noteToDelete = note },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                // Body: Decrypted Content
                                                Text(
                                                    text = note.decryptedContent,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Footer: Encrypted Ciphertext preview chip
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Lock,
                                                            contentDescription = "Encrypted",
                                                            modifier = Modifier.size(12.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "Ciphertext: ${note.encryptedContent}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontFamily = FontFamily.Monospace,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ─── FOOTER LINK ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GitHub Source Code",
                    style = MaterialTheme.typography.bodySmall.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            uriHandler.openUri("https://github.com/shdinu/CleanVaultAndroidApp")
                        }
                        .padding(4.dp)
                )
            }
        }
    }

    // ─── DIALOG 1: Add / Edit Note ───────────────────────────────────────────
    if (showAddEditDialog) {
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = {
                Text(if (editingNote == null) "Add New Vault Note" else "Edit Vault Note")
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteContentInput,
                        onValueChange = { noteContentInput = it },
                        label = { Text("Note Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = noteTitleInput.trim()
                        val content = noteContentInput.trim()
                        if (title.isBlank() || content.isBlank()) {
                            Toast.makeText(context, "Title and content cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val currentNote = editingNote
                        if (currentNote == null) {
                            viewModel.onAddNote(title, content) {
                                Toast.makeText(context, "Note encrypted & saved", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.onUpdateNote(currentNote.id, title, content) {
                                Toast.makeText(context, "Note updated & re-encrypted", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showAddEditDialog = false
                    }
                ) {
                    Text(if (editingNote == null) "Encrypt & Save" else "Update Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ─── DIALOG 2: Delete Note Confirmation ─────────────────────────────────
    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note?") },
            text = { Text("Are you sure you want to delete '${note.title}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteNote(note.id)
                        noteToDelete = null
                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}