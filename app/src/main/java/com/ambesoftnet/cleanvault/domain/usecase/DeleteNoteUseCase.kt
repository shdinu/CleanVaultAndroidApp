package com.ambesoftnet.cleanvault.domain.usecase

import com.ambesoftnet.cleanvault.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * # DeleteNoteUseCase — Domain Business Logic for Deleting a Note
 *
 * Invokes the repository to delete a note by its unique ID.
 */
class DeleteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Executes the use case.
     *
     * @param id The unique ID of the note to delete
     */
    suspend operator fun invoke(id: Int) {
        repository.deleteNote(id)
    }
}
