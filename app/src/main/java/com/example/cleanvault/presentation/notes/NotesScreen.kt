package com.example.cleanvault.presentation.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * # NotesScreen — Jetpack Compose UI for the Notes Feature
 *
 * This is the **entire UI** for the notes screen, written as a single composable function.
 * There is no XML layout file, no `ViewBinding`, and no `RecyclerView.Adapter` — Compose
 * replaces all of that with simple Kotlin functions.
 *
 * ---
 *
 * ## What is Jetpack Compose?
 *
 * Jetpack Compose is Android's **declarative UI framework**. Instead of describing
 * *how to change* the UI imperatively (`textView.text = "Hello"`), you describe
 * *what the UI should look like* given the current state:
 *
 * ```kotlin
 * @Composable
 * fun Greeting(name: String) {
 *     Text("Hello, $name!")
 * }
 * ```
 *
 * When `name` changes, Compose **automatically re-renders** the affected parts.
 * You never call `invalidate()` or `notifyDataSetChanged()`.
 *
 * ---
 *
 * ## MVVM + Compose: Unidirectional Data Flow
 *
 * ```
 * NotesViewModel ──── emits ────→ NotesUiState (StateFlow)
 *     ↑                                 ↓
 * user events           collectAsState() → recomposition
 *     ↑                                 ↓
 * NotesScreen ←──── reads ────── UI renders based on state
 * ```
 *
 * 1. `viewModel.uiState` is a `StateFlow<NotesUiState>`.
 * 2. `collectAsState()` subscribes the composable to the flow.
 * 3. When the state changes, only the Composable functions that **read** the changed
 *    state are **recomposed** (re-rendered) — Compose is very efficient.
 *
 * ---
 *
 * ## Key Compose Components Used
 *
 * | Component | Purpose |
 * |---|---|
 * | `Scaffold` | Provides the app bar + content area layout |
 * | `TopAppBar` | Material 3 top app bar with a title |
 * | `Card` | Elevated container card for note items |
 * | `OutlinedTextField` | Text input field with a border |
 * | `Button` | Clickable Material 3 button |
 * | `LazyColumn` | Efficient scrollable list (like `RecyclerView`) |
 * | `CircularProgressIndicator` | Loading spinner |
 * | `Text` | Display text with Material 3 typography |
 * | `Column` / `Row` | Vertical / horizontal layout containers |
 * | `Spacer` | Empty space for padding between elements |
 *
 * ---
 *
 * ## State Management in Compose
 *
 * ```kotlin
 * var secretText by remember { mutableStateOf("") }
 * ```
 *
 * - `mutableStateOf("")` — creates an observable state holder (starts empty).
 * - `remember { }` — keeps the state alive across **recompositions** of this function.
 *   Without `remember`, every recomposition would reset `secretText` to `""`.
 * - `by` (Kotlin delegate) — allows reading/writing `secretText` directly instead of
 *   `secretText.value`.
 *
 * ---
 *
 * ## Rendering Different UI States
 *
 * ```kotlin
 * when (val state = uiState) {
 *     is NotesUiState.Loading -> CircularProgressIndicator(...)
 *     is NotesUiState.Error   -> Text("Error: ${state.message}", ...)
 *     is NotesUiState.Success -> { ... render notes list ... }
 * }
 * ```
 *
 * This `when` block is **exhaustive** — the Kotlin compiler ensures we handle every
 * possible state. If a new state is added to [NotesUiState], this file won't compile
 * until it's handled here. Safer than a chain of `if/else` checks.
 *
 * @param viewModel The [NotesViewModel] that drives this screen's state and handles events.
 *                  Injected by Hilt via `MainActivity.viewModels()`.
 *
 * @see NotesViewModel
 * @see NotesUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: NotesViewModel) {

    /**
     * Collect the [NotesViewModel.uiState] StateFlow as Compose State.
     *
     * `collectAsState()` subscribes this composable to the StateFlow.
     * Whenever the ViewModel emits a new [NotesUiState], this composable
     * automatically **recomposes** (re-renders) with the new value.
     *
     * The `by` keyword is a Kotlin **property delegate** that allows reading
     * `uiState` directly (instead of `uiState.value`).
     */
    val uiState by viewModel.uiState.collectAsState()

    /**
     * Local UI state for the secret text field.
     *
     * `remember` keeps this value alive across recompositions.
     * `mutableStateOf("")` initialises it as an empty, observable string.
     *
     * This is **ephemeral state** — it belongs to the UI (not the ViewModel) because
     * it's just what the user is currently typing. It doesn't need to survive
     * screen rotation (unlike data in the ViewModel).
     */
    var secretText by remember { mutableStateOf("") }

    /**
     * Scaffold provides the standard Material 3 screen structure:
     * - `topBar` → rendered at the top
     * - `content` (the lambda `{ padding -> ... }`) → the main scrollable area
     *
     * The `padding` parameter from the lambda accounts for the TopAppBar's height,
     * so our content doesn't get hidden behind it.
     */
    Scaffold(
        topBar = { TopAppBar(title = { Text("Clean Architecture Vault") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)           // Respect Scaffold's insets
                .padding(16.dp)             // Add our own 16dp margin
                .fillMaxSize()              // Take all available space
        ) {
            // ─── SECTION 1: Encrypted Storage Demo Card ─────────────────────
            /**
             * This Card demonstrates the full **encrypt → store → decrypt → display** cycle.
             *
             * The user types a secret, clicks "Encrypt & Save":
             * 1. [NotesViewModel.onSaveSecret] is called → [SaveSecretNoteUseCase] validates
             * 2. [SecureStorageManager] encrypts with AES-256-GCM → writes to disk
             * 3. Immediately decrypted → [NotesUiState.Success.decryptedSecret] updated
             * 4. Compose re-renders the "Decrypted Value" text below
             */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Encrypted Storage (KeyStore)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    /**
                     * Controlled text field — value is always driven by [secretText] state.
                     * `onValueChange` updates [secretText] on every keystroke, which
                     * triggers recomposition to show the new text in the field.
                     */
                    OutlinedTextField(
                        value = secretText,
                        onValueChange = { secretText = it },
                        label = { Text("Enter Secret Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    /**
                     * "Encrypt & Save" button.
                     * On click: sends the [secretText] to the ViewModel as an event.
                     * The ViewModel handles all logic — the UI just fires the event.
                     */
                    Button(
                        onClick = { viewModel.onSaveSecret("user_vault_key", secretText) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Encrypt & Save")
                    }
                }
            }

            // ─── SECTION 2: State-Driven Content ────────────────────────────
            /**
             * Exhaustive `when` on the sealed interface [NotesUiState].
             *
             * Using `val state = uiState` in the `when` allows smart-casting:
             * inside `is NotesUiState.Success -> { }`, `state` is automatically
             * cast to `NotesUiState.Success` so we can access `state.notes` directly.
             *
             * This pattern is idiomatic Kotlin and replaces verbose
             * `if (uiState is Success) { (uiState as Success).notes }` chains.
             */
            when (val state = uiState) {
                // Loading: show a centered spinner
                is NotesUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Error: show error message in Material error colour
                is NotesUiState.Error -> Text(
                    "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )

                // Success: show decrypted secret (if any) + notes list
                is NotesUiState.Success -> {
                    /**
                     * Show the decrypted secret value if one has been retrieved.
                     * `?.let { ... }` is Kotlin's safe-call — only executes the block
                     * if [NotesUiState.Success.decryptedSecret] is non-null.
                     */
                    state.decryptedSecret?.let { secret ->
                        Text(
                            "Decrypted Value: $secret",
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    /**
                     * LazyColumn — the Compose equivalent of RecyclerView.
                     *
                     * "Lazy" means only the items visible on screen are composed and rendered.
                     * Off-screen items are not in memory — making this efficient for large lists.
                     *
                     * `items(state.notes)` iterates the [NotesUiState.Success.notes] list
                     * and calls the lambda for each item.
                     */
                    LazyColumn {
                        items(state.notes) { note ->
                            /**
                             * Each note is displayed in a Material 3 [Card].
                             *
                             * `vertical = 4.dp` spacing between cards.
                             * Inside: a [Column] with the title (bold) and content (body).
                             */
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = note.title,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium
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