package com.example.cleanvault.domain.usecase

import com.example.cleanvault.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * # SaveSecretNoteUseCase — Business Logic: Validate & Encrypt Secret
 *
 * This Use Case encapsulates the business rule for saving a sensitive secret:
 * *"A secret must not be blank, and must be encrypted before being persisted."*
 *
 * ---
 *
 * ## Why a Separate Use Case for Saving?
 *
 * Even though this use case is currently small, it demonstrates an important principle:
 * **business validation lives in the Domain Layer, not the UI or Data Layer.**
 *
 * Consider what happens if validation was in the ViewModel:
 * - If you add a second screen that also saves secrets, you'd duplicate the validation.
 * - If you test the ViewModel, you test the validation indirectly (harder to isolate).
 *
 * By putting `require(secret.isNotBlank())` here:
 * - Any caller (any ViewModel, any screen) automatically gets the validation.
 * - You test the rule with ONE focused unit test on this class alone.
 *
 * ---
 *
 * ## OOP Concept: Single Responsibility Principle (S in SOLID)
 *
 * [SaveSecretNoteUseCase] does exactly one thing: validate and delegate the save operation.
 * It does NOT know about:
 * - Which encryption algorithm is used (AES-256 — that's [com.example.cleanvault.data.local.SecureStorageManager]'s job).
 * - How the ViewModel triggers this (that's [com.example.cleanvault.presentation.notes.NotesViewModel]'s job).
 * - Where the data is stored (that's [com.example.cleanvault.data.repository.NoteRepositoryImpl]'s job).
 *
 * ---
 *
 * ## OOP Concept: `suspend operator fun invoke()`
 *
 * Like [GetNotesUseCase], this use case overloads `invoke` for clean call-site syntax:
 * ```kotlin
 * saveSecretNoteUseCase("my_key", "my_secret_value")
 * // reads naturally as: "save the secret"
 * ```
 *
 * `suspend` is required because [NoteRepository.saveEncryptedSecret] is a `suspend`
 * function (it writes to disk on a background thread). This coroutine propagates the
 * suspension up to the ViewModel's `viewModelScope.launch {}`.
 *
 * ---
 *
 * ## Business Rule: Non-blank Validation
 *
 * ```kotlin
 * require(secret.isNotBlank()) { "Secret cannot be blank" }
 * ```
 *
 * [require] is a Kotlin standard library function that throws an
 * [IllegalArgumentException] if the condition is `false`. The message explains WHY.
 *
 * **Intern Tip:** `isNotBlank()` checks that the string is not empty AND not just whitespace.
 * `"   ".isNotBlank()` returns `false` — protecting against users entering only spaces.
 *
 * @param repository The [NoteRepository] interface injected by Hilt
 *
 * @see NoteRepository
 * @see com.example.cleanvault.presentation.notes.NotesViewModel.onSaveSecret
 * @see GetNotesUseCase
 */
class SaveSecretNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {

    /**
     * Validates and saves an encrypted secret to secure storage.
     *
     * ## Behaviour
     *
     * 1. **Validate** — throws [IllegalArgumentException] if [secret] is blank.
     * 2. **Delegate** — calls [NoteRepository.saveEncryptedSecret] which encrypts
     *    the value with AES-256-GCM and writes to `EncryptedSharedPreferences`.
     *
     * ## Calling Pattern
     *
     * ```kotlin
     * // In ViewModel:
     * viewModelScope.launch {
     *     saveSecretNoteUseCase(key = "vault_key", secret = "my_p@ssword")
     *     // After this returns, the secret is encrypted on disk
     * }
     * ```
     *
     * @param key    The unique identifier under which the secret is stored
     * @param secret The plaintext secret to validate and encrypt. Must not be blank.
     *
     * @throws IllegalArgumentException if [secret] is blank or contains only whitespace
     */
    suspend operator fun invoke(key: String, secret: String) {
        require(secret.isNotBlank()) { "Secret cannot be blank" }
        repository.saveEncryptedSecret(key, secret)
    }
}