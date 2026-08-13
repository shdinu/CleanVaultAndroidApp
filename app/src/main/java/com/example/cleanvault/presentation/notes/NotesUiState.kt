package com.example.cleanvault.presentation.notes

import com.example.cleanvault.domain.model.Note

/**
 * # NotesUiState — UI State Model for the Notes Screen
 *
 * This **sealed interface** models every possible state that the Notes screen can be in.
 * The ViewModel emits one of these states, and the Compose UI renders accordingly.
 *
 * ---
 *
 * ## What is a Sealed Interface?
 *
 * A `sealed interface` (or `sealed class`) is a **closed set of types** — like an
 * enum on steroids. Every possible subtype must be declared in the same file.
 *
 * **Why this matters for the UI:**
 * ```kotlin
 * when (uiState) {
 *     is NotesUiState.Loading -> ShowSpinner()
 *     is NotesUiState.Success -> ShowNotes(uiState.notes)
 *     is NotesUiState.Error   -> ShowError(uiState.message)
 * }
 * ```
 * The Kotlin compiler guarantees this `when` is **exhaustive** — you MUST handle
 * every case. If you add a new state (e.g., `Empty`), the compiler will force you
 * to handle it in the UI. No silent bugs!
 *
 * ---
 *
 * ## OOP Concept: Polymorphism
 *
 * All three variants (`Loading`, `Success`, `Error`) implement the same
 * [NotesUiState] interface. The ViewModel exposes a single `StateFlow<NotesUiState>`,
 * and the UI uses polymorphic dispatch (`when`) to render the right content.
 *
 * ---
 *
 * ## MVVM Architecture: Unidirectional Data Flow (UDF)
 *
 * ```
 * User Action → ViewModel → emits new NotesUiState → UI re-renders
 * ```
 * The UI never modifies state directly. It only reads [NotesUiState] and sends
 * events (like button clicks) back to the ViewModel. This is **Unidirectional
 * Data Flow** — a key principle of MVVM with Compose.
 *
 * ---
 *
 * ## States Explained
 *
 * | State | When Emitted | UI Action |
 * |---|---|---|
 * | [Loading] | Initial state, before any data arrives | Show `CircularProgressIndicator` |
 * | [Success] | Notes loaded from DB + (optional) secret decrypted | Show note list + decrypted value |
 * | [Error] | An exception was thrown in the data pipeline | Show error message in red |
 *
 * @see NotesViewModel
 * @see NotesScreen
 */
sealed interface NotesUiState {

    /**
     * The screen is loading data for the first time.
     *
     * This is the **initial state** emitted by [NotesViewModel.uiState] before any
     * notes arrive from the Room database Flow.
     *
     * In [NotesScreen], this triggers a `CircularProgressIndicator` centered on screen.
     *
     * Implemented as a Kotlin `object` (singleton) because [Loading] carries no data —
     * all `Loading` instances are identical, so there's no need to create multiple instances.
     */
    object Loading : NotesUiState

    /**
     * Notes have been successfully loaded and are ready to display.
     *
     * This state is emitted by the ViewModel's `combine()` flow whenever either
     * the list of notes OR the decrypted secret changes.
     *
     * @property notes           The list of [Note] domain objects to display
     * @property decryptedSecret The plaintext secret retrieved from encrypted storage,
     *                           or `null` if no secret has been saved/retrieved yet
     */
    data class Success(
        /** The current list of notes to render in the LazyColumn. */
        val notes: List<Note>,

        /**
         * The decrypted secret value, shown at the top of the screen.
         * `null` initially — becomes non-null after the user saves and retrieves a secret.
         * This demonstrates the full encrypt → store → decrypt → display cycle.
         */
        val decryptedSecret: String? = null
    ) : NotesUiState

    /**
     * An error occurred while loading data (e.g., database exception, network failure).
     *
     * The ViewModel's `.catch {}` operator intercepts uncaught exceptions from the
     * `Flow` pipeline and emits this state instead of crashing the app.
     *
     * In [NotesScreen], this shows an error message styled with `MaterialTheme.colorScheme.error`.
     *
     * @property message A human-readable description of the error. Defaults to
     *                   `"An unexpected error occurred"` if the exception has no message.
     */
    data class Error(
        /** The error message to display to the user. */
        val message: String
    ) : NotesUiState
}