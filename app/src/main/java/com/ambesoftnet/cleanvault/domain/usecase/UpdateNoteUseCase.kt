package com.ambesoftnet.cleanvault.domain.usecase

import com.ambesoftnet.cleanvault.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * # UpdateNoteUseCase — Domain Business Logic for Updating an Existing Encrypted Note
 *
 * Validates updated note fields and invokes the repository to re-encrypt and persist.
 */
class UpdateNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Executes the use case.
     *
     * @param id           The unique ID of the note to update
     * @param title        The updated note title (must not be blank)
     * @param plainContent The updated note body content (must not be blank)
     * @throws IllegalArgumentException if title or content is blank
     */
    suspend operator fun invoke(id: Int, title: String, plainContent: String) {
        val trimmedTitle = title.trim()
        val trimmedContent = plainContent.trim()

        require(trimmedTitle.isNotBlank()) { "Note title cannot be blank" }
        require(trimmedContent.isNotBlank()) { "Note content cannot be blank" }

        repository.updateEncryptedNote(id, trimmedTitle, trimmedContent)
    }
}
