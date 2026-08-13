package com.example.cleanvault.data.repository

import com.example.cleanvault.data.local.NoteDao
import com.example.cleanvault.data.local.NoteEntity
import com.example.cleanvault.data.local.SecureStorageManager
import com.example.cleanvault.data.remote.NoteApi
import com.example.cleanvault.domain.model.Note
import com.example.cleanvault.domain.repository.NoteRepository
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
     * ## Data Transformation (Mapping)
     *
     * Room returns `Flow<List<NoteEntity>>` (Data Layer model).
     * This function transforms it to `Flow<List<Note>>` (Domain Layer model)
     * using [Flow.map] — a Kotlin Flow operator:
     *
     * ```
     * Flow<List<NoteEntity>>
     *   .map { entities ->
     *       entities.map { entity -> Note(entity.id, entity.title, entity.content, ...) }
     *   }
     * → Flow<List<Note>>
     * ```
     *
     * The Domain Layer never sees [NoteEntity]. It only works with [Note].
     *
     * ## Why Flow?
     *
     * The [Flow] from Room is "live" — it automatically emits a new [List] every time
     * the database is updated. The ViewModel observes this stream, and Compose
     * re-renders the UI automatically.
     *
     * @return A [Flow] that emits a new [List] of domain [Note] objects whenever
     *         the local `notes` table is modified
     */
    override fun getNotes(): Flow<List<Note>> {
        return dao.getAllNotes().map { entities ->
            entities.map { Note(it.id, it.title, it.content, it.isEncrypted) }
        }
    }

    /**
     * Fetches the latest notes from the remote API and syncs them into the local database.
     *
     * ## Offline-First Strategy
     *
     * This function implements a simple **sync pattern**:
     * 1. Fetch from remote (Retrofit GET /posts)
     * 2. Take the first 10 results (to keep the demo manageable)
     * 3. Map [NoteDto] → [NoteEntity]
     * 4. Insert into Room (REPLACE on conflict — remote wins)
     *
     * After insertion, the [NoteDao.getAllNotes] Flow automatically emits the
     * new data — no manual notification needed.
     *
     * ## Error Handling
     *
     * A `try/catch` wraps the entire operation. If the network is unavailable or
     * the server returns an error, the exception is caught and logged instead of
     * crashing the app. The local database data (if any) remains intact.
     *
     * ## Coroutines: `withContext`
     *
     * `withContext(ioDispatcher)` ensures this entire function runs on the
     * background thread pool (`Dispatchers.IO`), not the UI thread.
     * The `suspend` keyword means the calling coroutine is suspended (not blocked)
     * while this executes.
     */
    override suspend fun fetchAndSyncNotes() = withContext(ioDispatcher) {
        try {
            val remoteNotes = api.getRemoteNotes()
            val entities = remoteNotes.take(10).map {
                NoteEntity(id = it.id, title = it.title, content = it.body, isEncrypted = false)
            }
            dao.insertNotes(entities)
        } catch (e: Exception) {
            // In production, use a logging framework (Timber) and propagate
            // meaningful errors to the ViewModel via a Result wrapper
            e.printStackTrace()
        }
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