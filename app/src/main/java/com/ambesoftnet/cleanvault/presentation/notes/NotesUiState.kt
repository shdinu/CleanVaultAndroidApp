package com.ambesoftnet.cleanvault.presentation.notes

import com.ambesoftnet.cleanvault.domain.model.Note

/**
 * # NotesUiState — UI State Model for the Notes Screen
 *
 * This **sealed interface** models every possible state that the Notes screen can be in.
 * The ViewModel emits one of these states, and the Compose UI renders accordingly.
 */
sealed interface NotesUiState {

    /**
     * The screen is loading data for the first time.
     */
    object Loading : NotesUiState

    /**
     * Notes have been successfully loaded and are ready to display.
     *
     * @property notes           The filtered/full list of [Note] domain objects to display
     * @property allNotes        The unfiltered full list of notes
     * @property decryptedSecret The plaintext secret retrieved from encrypted storage, or null
     * @property searchQuery     The active search filter text
     */
    data class Success(
        val notes: List<Note>,
        val allNotes: List<Note> = notes,
        val decryptedSecret: String? = null,
        val searchQuery: String = ""
    ) : NotesUiState

    /**
     * An error occurred while loading data or performing a vault operation.
     */
    data class Error(
        val message: String
    ) : NotesUiState
}
