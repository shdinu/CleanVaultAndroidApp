package com.example.cleanvault.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleanvault.domain.repository.NoteRepository
import com.example.cleanvault.domain.usecase.GetNotesUseCase
import com.example.cleanvault.domain.usecase.SaveSecretNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * # NotesViewModel — MVVM ViewModel for the Notes Screen
 *
 * The ViewModel is the **brain** of the MVVM (Model-View-ViewModel) architecture.
 * It sits between the UI (View = [NotesScreen]) and the business logic (Model = Use Cases).
 *
 * ---
 *
 * ## What is a ViewModel?
 *
 * A [ViewModel] is a Jetpack component that:
 * 1. **Survives configuration changes** (screen rotation, language change).
 *    When the Activity is re-created, the ViewModel is NOT re-created.
 *    Your data and coroutines are preserved.
 * 2. **Owns the coroutine scope** (`viewModelScope`) — all coroutines are automatically
 *    cancelled when the ViewModel is destroyed (when the user leaves the screen permanently).
 *    This prevents **memory leaks** and **zombie background threads**.
 * 3. **Holds UI state** — exposes a [StateFlow] that the UI observes reactively.
 *
 * ---
 *
 * ## MVVM Data Flow
 *
 * ```
 * [NotesScreen] ←collectAsState()← [uiState: StateFlow]
 *                                          ↑
 * [NotesScreen] ──button click──→ [onSaveSecret()] ──→ [SaveSecretNoteUseCase]
 *                                          |                      |
 *                                   [viewModelScope]         [NoteRepository]
 *                                          |                      |
 *                                   [getNotesUseCase()] ─────→ Room DB Flow
 * ```
 *
 * ---
 *
 * ## Coroutines: Key Flow Concepts
 *
 * ### `combine(flow1, flow2) { a, b -> ... }`
 * Merges two separate `Flow`s into one. Every time either `flow1` OR `flow2` emits
 * a new value, the lambda runs and produces a combined result.
 *
 * In this ViewModel:
 * - `flow1` = `getNotesUseCase()` → emits new note list from Room on every DB change
 * - `flow2` = `_decryptedSecret` → emits whenever user decrypts a secret
 * → Combined into `NotesUiState.Success`
 *
 * ### `.catch { }`
 * If any exception is thrown in the upstream flow, `.catch { }` intercepts it and
 * emits a safe `NotesUiState.Error` — instead of crashing the app.
 *
 * ### `.stateIn(scope, started, initialValue)`
 * Converts a cold `Flow` into a hot `StateFlow`:
 * - `scope = viewModelScope` — the flow lives as long as the ViewModel.
 * - `started = SharingStarted.WhileSubscribed(5000)` — start collecting upstream only while
 *   the UI is subscribed. Stop after 5 seconds of no subscribers (background → save battery).
 * - `initialValue = NotesUiState.Loading` — what the UI sees before the first emit.
 *
 * ---
 *
 * ## OOP Concepts
 *
 * - **Inheritance:** `NotesViewModel : ViewModel()` — gains lifecycle awareness.
 * - **Encapsulation:** `_decryptedSecret` is `private`. External code only sees the
 *   immutable [uiState] — they cannot mutate state directly.
 * - **Dependency Injection:** `@HiltViewModel` + `@Inject constructor` — Hilt provides all
 *   dependencies. Never create `NotesViewModel(...)` manually.
 *
 * @param getNotesUseCase       Use case for observing the live list of notes
 * @param saveSecretNoteUseCase Use case for validating and encrypting a secret
 * @param repository            Direct repository access for sync and decrypt operations
 *
 * @see NotesScreen
 * @see NotesUiState
 * @see GetNotesUseCase
 * @see SaveSecretNoteUseCase
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val saveSecretNoteUseCase: SaveSecretNoteUseCase,
    private val repository: NoteRepository
) : ViewModel() {

    /**
     * Internal mutable state holding the currently decrypted secret value.
     *
     * This is a [MutableStateFlow] — a [StateFlow] that this ViewModel can emit new values into.
     * External code (including [NotesScreen]) sees only the read-only [uiState] StateFlow,
     * which includes [_decryptedSecret] as part of [NotesUiState.Success].
     *
     * **Encapsulation pattern:** `private val _decryptedSecret` → exposed via immutable `uiState`.
     * This is the standard Kotlin "backing property" pattern for state management.
     *
     * Initially `null` — becomes non-null after [onDecryptSecret] runs successfully.
     */
    private val _decryptedSecret = MutableStateFlow<String?>(null)

    /**
     * The single source of truth for the UI state of the Notes screen.
     *
     * This [StateFlow] is observed by [NotesScreen] via `collectAsState()`.
     * Every emission causes Compose to **recompose** (re-render) the affected parts of the UI.
     *
     * ## How It's Built
     *
     * ```kotlin
     * combine(
     *     getNotesUseCase(),    // Flow<List<Note>> from Room
     *     _decryptedSecret      // Flow<String?> from secure storage
     * ) { notes, secret ->
     *     NotesUiState.Success(notes, secret)  // Combine into a single Success state
     * }
     * .catch { NotesUiState.Error(it.message) } // Safe error handling
     * .stateIn(viewModelScope, WhileSubscribed(5000), Loading)
     * ```
     *
     * ## State Transitions
     *
     * ```
     * App starts → Loading (initial)
     *   ↓ Room emits first list (even empty)
     * Success(notes = [], secret = null)
     *   ↓ fetchAndSyncNotes() inserts 10 notes from API
     * Success(notes = [10 items], secret = null)
     *   ↓ User types secret, clicks "Encrypt & Save"
     * Success(notes = [10 items], secret = "user's decrypted secret")
     * ```
     */
    val uiState: StateFlow<NotesUiState> = combine(
        getNotesUseCase(),
        _decryptedSecret
    ) { notes, secret ->
        NotesUiState.Success(notes = notes, decryptedSecret = secret) as NotesUiState
    }.catch {
        emit(NotesUiState.Error(it.message ?: "An unexpected error occurred"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState.Loading
    )

    /**
     * Automatically called when this ViewModel is first created.
     *
     * Triggers [syncRemoteData] to fetch the latest notes from the remote API and
     * sync them into the local Room database. This happens in the background — the
     * UI shows [NotesUiState.Loading] until Room emits its first data.
     */
    init {
        syncRemoteData()
    }

    /**
     * Launches a coroutine to fetch remote notes and sync to the local database.
     *
     * ## Coroutines: `viewModelScope.launch`
     *
     * `viewModelScope` is a [kotlinx.coroutines.CoroutineScope] that is:
     * - Bound to the ViewModel's lifecycle.
     * - Cancelled automatically when `onCleared()` is called (ViewModel destroyed).
     * - Uses `Dispatchers.Main.immediate` by default, but the repository switches to
     *   `Dispatchers.IO` for the actual network/DB work via `withContext`.
     *
     * `launch {}` starts a **fire-and-forget coroutine** — the function returns immediately
     * and the coroutine runs concurrently in the background.
     */
    private fun syncRemoteData() {
        viewModelScope.launch {
            repository.fetchAndSyncNotes()
        }
    }

    /**
     * Called when the user clicks "Encrypt & Save" in [NotesScreen].
     *
     * ## Sequence of Events
     *
     * 1. Launch a coroutine on `viewModelScope`.
     * 2. Call [SaveSecretNoteUseCase] — validates the secret is non-blank, then
     *    encrypts and writes it via [com.example.cleanvault.data.local.SecureStorageManager].
     * 3. Immediately call [onDecryptSecret] with the same key — retrieves the decrypted
     *    value and updates `_decryptedSecret`, which triggers the UI to re-render with
     *    the decrypted value shown.
     *
     * This demonstrates the full **encrypt → store → decrypt → display** cycle.
     *
     * @param key    The storage key (e.g., `"user_vault_key"`)
     * @param secret The plaintext secret the user typed into the text field
     */
    fun onSaveSecret(key: String, secret: String) {
        viewModelScope.launch {
            saveSecretNoteUseCase(key, secret)
            onDecryptSecret(key)
        }
    }

    /**
     * Retrieves and decrypts the secret stored under [key], then updates the UI state.
     *
     * ## Flow
     *
     * 1. Launch a coroutine on `viewModelScope`.
     * 2. Call `repository.getDecryptedSecret(key)` — runs on `Dispatchers.IO`.
     * 3. Update `_decryptedSecret.value` with the result.
     * 4. The `combine()` in [uiState] detects the change and emits a new `Success` state.
     * 5. Compose re-renders [NotesScreen] with the decrypted value shown.
     *
     * The entire journey from disk → UI happens **reactively** without any manual
     * "refresh" calls or callback chains.
     *
     * @param key The storage key whose secret should be decrypted and displayed
     */
    fun onDecryptSecret(key: String) {
        viewModelScope.launch {
            _decryptedSecret.value = repository.getDecryptedSecret(key)
        }
    }
}