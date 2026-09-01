package com.ambesoftnet.cleanvault.data.repository

import com.ambesoftnet.cleanvault.data.local.NoteDao
import com.ambesoftnet.cleanvault.data.local.NoteEntity
import com.ambesoftnet.cleanvault.data.local.SecureStorageManager
import com.ambesoftnet.cleanvault.data.remote.NoteApi
import com.ambesoftnet.cleanvault.domain.model.Note
import com.ambesoftnet.cleanvault.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * # NoteRepositoryImpl — Concrete Repository Implementation
 *
 * This class is the **heart of the Data Layer**. It implements the [NoteRepository]
 * interface (defined in the Domain Layer) and orchestrates all data operations:
 * - Fetching data from the remote API (Retrofit)
 * - Storing and querying data from the local database (Room)
 * - Encrypting/decrypting sensitive data (Android KeyStore via [SecureStorageManager])
 *
 * ---
 *
 * ## Clean Architecture: The Repository Pattern
 *
 * The **Repository Pattern** is the bridge between the Data Layer and the Domain Layer.
 *
 * ```
 * Domain Layer (UseCase)        ← depends on →        NoteRepository (interface)
 *                                                              ↑
 *                                                    implements (hidden from Domain)
 *                                                              |
 * Data Layer (Retrofit + Room)  ← managed by →   NoteRepositoryImpl (this class)
 * ```
 *
 * The Domain Layer **never knows** about Retrofit, Room, or encryption. It only
 * knows the [NoteRepository] interface contract — this is **Dependency Inversion**.
 *
 * ---
 *
 * ## OOP Concepts
 *
 * - **Inheritance / Interface Implementation:** `NoteRepositoryImpl : NoteRepository`
 *   — this class fulfils the contract defined by the interface.
 * - **Constructor Injection (Hilt):** All 4 dependencies are injected via constructor.
 *   `@Inject` tells Hilt: "When you create this class, pass these dependencies in."
 * - **Single Responsibility:** This class manages note data. Period.
 * - **Encapsulation:** `api`, `dao`, `secureStorage`, `ioDispatcher` are all `private`.
 *
 * ---
 *
 * ## Multithreading Strategy
 *
 * All heavy operations (network calls, disk I/O) use `withContext(ioDispatcher)`:
 * ```kotlin
 * override suspend fun fetchAndSyncNotes() = withContext(ioDispatcher) {
 *     // This code block runs on Dispatchers.IO (background thread pool)
 *     // The calling coroutine is suspended here, not blocked
 * }
 * ```
 *
 * `withContext` switches the coroutine to the specified dispatcher for the duration
 * of the block, then automatically switches back to the original dispatcher.
 * This ensures the UI thread (Main) is never blocked.
 *
 * @param api          Retrofit [NoteApi] for making HTTP GET requests
 * @param dao          Room [NoteDao] for reading/writing the local `notes` table
 * @param secureStorage [SecureStorageManager] for AES-256 encrypted key-value storage
 * @param ioDispatcher [CoroutineDispatcher] for background I/O thread execution
 *
 * @see NoteRepository
 * @see NoteApi
 * @see NoteDao
 * @see SecureStorageManager
 */
class NoteRepositoryImpl @Inject constructor(
    private val api: NoteApi,
    private val dao: NoteDao,
    private val secureStorage: SecureStorageManager,
    private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    /**
     * Returns a reactive stream of all notes from the local Room database.
     *
     * In the database, content is stored strictly in encrypted ciphertext format.
     * When emitting to the Domain layer, this method transparently decrypts
     * the content so that both [Note.encryptedContent] and [Note.decryptedContent]
     * are available.
     *
     * @return A [Flow] that emits a new [List] of domain [Note] objects whenever
     *         the local `notes` table is modified
     */
    override fun getNotes(): Flow<List<Note>> {
        return dao.getAllNotes().map { entities ->
            entities.map { entity ->
                val encryptedText = entity.content
                val decryptedText = if (entity.isEncrypted) {
                    secureStorage.decrypt(entity.content)
                } else {
                    entity.content
                }
                Note(
                    id = entity.id,
                    title = entity.title,
                    encryptedContent = encryptedText,
                    decryptedContent = decryptedText,
                    isEncrypted = entity.isEncrypted
                )
            }
        }
    }

    /**
     * Encrypts plaintext and saves ONLY the encrypted ciphertext to the local Room database.
     */
    override suspend fun saveEncryptedNote(title: String, plainContent: String): Unit = withContext(ioDispatcher) {
        val ciphertext = secureStorage.encrypt(plainContent)
        val entity = NoteEntity(
            id = 0,
            title = title,
            content = ciphertext,
            isEncrypted = true
        )
        dao.insertNote(entity)
    }

    /**
     * Updates an existing note by encrypting its content and updating the Room entity.
     */
    override suspend fun updateEncryptedNote(id: Int, title: String, plainContent: String): Unit = withContext(ioDispatcher) {
        val ciphertext = secureStorage.encrypt(plainContent)
        val entity = NoteEntity(
            id = id,
            title = title,
            content = ciphertext,
            isEncrypted = true
        )
        dao.updateNote(entity)
    }

    /**
     * Deletes a note by its unique identifier.
     */
    override suspend fun deleteNote(id: Int): Unit = withContext(ioDispatcher) {
        dao.deleteNoteById(id)
    }

    /**
     * Clears all notes from the local database.
     */
    override suspend fun clearAllNotes(): Unit = withContext(ioDispatcher) {
        dao.clearAllNotes()
    }

    /**
     * Syncs remote notes if applicable.
     * Placeholder Latin dummy posts from JSONPlaceholder are no longer synced.
     */
    override suspend fun fetchAndSyncNotes() = withContext(ioDispatcher) {
        // No longer syncing placeholder dummy posts into the local database
    }

    /**
     * Encrypts and saves a secret value under the given key.
     *
     * Delegates to [SecureStorageManager.saveSecret], which uses
     * `EncryptedSharedPreferences` backed by the Android KeyStore.
     *
     * The plaintext [secret] is **never written to disk** — only the
     * AES-256-GCM encrypted ciphertext is persisted.
     *
     * `withContext(ioDispatcher)` ensures the I/O operation (writing to disk)
     * happens on a background thread.
     *
     * @param key    A unique string key to store the secret under
     * @param secret The plaintext secret to encrypt and persist
     */
    override suspend fun saveEncryptedSecret(key: String, secret: String) = withContext(ioDispatcher) {
        secureStorage.saveSecret(key, secret)
    }

    /**
     * Decrypts and retrieves the secret stored under the given key.
     *
     * Delegates to [SecureStorageManager.getSecret], which uses the Android
     * KeyStore master key to decrypt the stored ciphertext.
     *
     * The decryption and disk read happen on the background thread (`ioDispatcher`).
     * The plaintext result is returned to the calling coroutine (typically the ViewModel),
     * which then emits it to the UI via [kotlinx.coroutines.flow.StateFlow].
     *
     * @param key The unique string key used when the secret was saved
     * @return The decrypted plaintext string, or `null` if no secret exists for [key]
     */
    override suspend fun getDecryptedSecret(key: String): String? = withContext(ioDispatcher) {
        secureStorage.getSecret(key)
    }
}
