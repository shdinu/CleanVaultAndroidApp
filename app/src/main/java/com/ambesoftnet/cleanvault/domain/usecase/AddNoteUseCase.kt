package com.ambesoftnet.cleanvault.domain.usecase

import com.ambesoftnet.cleanvault.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * # AddNoteUseCase — Domain Business Logic for Creating an Encrypted Note
 *
 * Validates note fields and invokes the repository to encrypt and save to local storage.
 */
class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    /**
     * Executes the use case.
     *
     * @param title        The note title (must not be blank)
     * @param plainContent The note body content (must not be blank)
     * @throws IllegalArgumentException if title or content is blank
     */
    suspend operator fun invoke(title: String, plainContent: String) {
        val trimmedTitle = title.trim()
        val trimmedContent = plainContent.trim()

        require(trimmedTitle.isNotBlank()) { "Note title cannot be blank" }
        require(trimmedContent.isNotBlank()) { "Note content cannot be blank" }

        repository.saveEncryptedNote(trimmedTitle, trimmedContent)
    }
}
