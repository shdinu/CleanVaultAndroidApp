package com.ambesoftnet.cleanvault.domain.repository

import com.ambesoftnet.cleanvault.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * # NoteRepository — Domain Repository Interface (Contract)
 *
 * This interface defines the **complete contract** for all note-related data operations
 * that the Domain Layer requires. It lives in the Domain Layer and is the boundary
 * between the Domain and Data layers.
 *
 * ---
 *
 * ## What is the Repository Pattern?
 *
 * The **Repository Pattern** abstracts the data sources (API, database, cache) behind
 * a single, clean interface. The Domain Layer (UseCases, ViewModel) only calls methods
 * on this interface — it never knows:
 * - Is data coming from Room? Retrofit? A cache? A file?
 * - On which thread does this run?
 * - How is encryption handled?
 *
 * This is exactly the **Dependency Inversion Principle (D in SOLID)**:
 *
 * ```
 * High-level module (UseCase) → depends on → Abstraction (NoteRepository interface)
 *                                                         ↑
 *                                          Low-level module (NoteRepositoryImpl)
 * ```
 *
 * ---
 *
 * ## OOP Concept: Interface
 *
 * An `interface` in Kotlin/Java is a **pure contract**. It:
 * - Declares method signatures without bodies.
 * - Can be implemented by any number of concrete classes.
 * - Enables **polymorphism** — you can swap implementations at runtime (or in tests).
 *
 * In production: Hilt injects [com.ambesoftnet.cleanvault.data.repository.NoteRepositoryImpl].
 * In unit tests: You inject a fake/mock that returns predefined data instantly.
 *
 * ---
 *
 * ## Clean Architecture: Why Interface in Domain Layer?
 *
 * The interface lives in `domain.repository` (Domain Layer) so that:
 * - The Domain Layer depends on **nothing** from the Data Layer.
 * - The Data Layer depends on the Domain Layer to implement this interface.
 * - This is called **Dependency Inversion** — dependencies point inward.
 *
 * ---
 *
 * ## Coroutines Design
 *
 * - [getNotes] returns a `Flow` — non-blocking, reactive, no `suspend` needed.
 * - [fetchAndSyncNotes], [saveEncryptedSecret], [getDecryptedSecret] are `suspend`
 *   — they are one-shot operations that complete and return, running on background threads.
 *
 * @see com.ambesoftnet.cleanvault.data.repository.NoteRepositoryImpl
 * @see com.ambesoftnet.cleanvault.domain.usecase.GetNotesUseCase
 * @see com.ambesoftnet.cleanvault.domain.usecase.SaveSecretNoteUseCase
 */
interface NoteRepository {

    /**
     * Returns a reactive stream of all notes from the local data source.
     *
     * The [Flow] emits a new [List] of [Note] objects whenever the underlying
     * data changes (e.g., after [fetchAndSyncNotes] inserts new notes).
     *
     * This is a **cold flow** — it only starts executing when a collector subscribes to it
     * (e.g., when the ViewModel calls `stateIn(viewModelScope, ...)`).
     *
     * @return A [Flow] that continuously emits the latest list of [Note] objects
     */
    fun getNotes(): Flow<List<Note>>

    /**
     * Fetches notes from the remote API and saves them to the local database.
     *
     * This is a **fire-and-forget sync operation**:
     * 1. HTTP GET → parse JSON → [List] of [com.ambesoftnet.cleanvault.data.remote.NoteDto]
     * 2. Map DTOs → [com.ambesoftnet.cleanvault.data.local.NoteEntity] objects
     * 3. Insert into Room → triggers [getNotes] Flow to emit fresh data
     *
     * Marked `suspend` — must be called from a coroutine scope (e.g., [kotlinx.coroutines.CoroutineScope.launch]).
     * The actual I/O happens on a background thread inside the implementation.
     */
    suspend fun fetchAndSyncNotes()

    /**
     * Encrypts and persists a secret value under the given key.
     *
     * The implementation uses [com.ambesoftnet.cleanvault.data.local.SecureStorageManager]
     * to store the value with AES-256-GCM encryption backed by the Android KeyStore.
     *
     * Marked `suspend` — the disk I/O runs on a background thread.
     *
     * @param key    The unique identifier for this secret (e.g., `"user_vault_key"`)
     * @param secret The plaintext value to encrypt and store
     */
    suspend fun saveEncryptedSecret(key: String, secret: String)

    /**
     * Encrypts the [plainContent] and persists it in the local Room database.
     * Only the encrypted ciphertext is saved to SQLite.
     *
     * @param title        The title/label for the note
     * @param plainContent The plaintext content to encrypt and store
     */
    suspend fun saveEncryptedNote(title: String, plainContent: String)

    /**
     * Updates an existing note by re-encrypting the [plainContent] and updating the database row.
     *
     * @param id           The ID of the note to update
     * @param title        The new title
     * @param plainContent The new plaintext content to encrypt and store
     */
    suspend fun updateEncryptedNote(id: Int, title: String, plainContent: String)

    /**
     * Deletes a note by its unique identifier.
     *
     * @param id The ID of the note to delete
     */
    suspend fun deleteNote(id: Int)

    /**
     * Clears all notes from local storage.
     */
    suspend fun clearAllNotes()

    /**
     * Decrypts and retrieves the secret associated with the given key.
     *
     * The decryption happens transparently via [com.ambesoftnet.cleanvault.data.local.SecureStorageManager].
     * The returned value is the original plaintext — ready to display in the UI.
     *
     * Marked `suspend` — the disk I/O and decryption run on a background thread.
     *
     * @param key The unique identifier used when the secret was saved
     * @return The decrypted plaintext string, or `null` if no secret is stored for [key]
     */
    suspend fun getDecryptedSecret(key: String): String?
}
