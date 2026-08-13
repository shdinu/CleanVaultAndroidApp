package com.example.cleanvault.domain.model

/**
 * # Note — Domain Model (Business Entity)
 *
 * This `data class` is the **pure business representation** of a note in the
 * CleanVault application. It is the central model that flows through the entire
 * Domain Layer and up to the Presentation Layer.
 *
 * ---
 *
 * ## What is a Domain Model?
 *
 * In **Clean Architecture**, every layer has its own data model:
 *
 * | Layer | Model | Has Framework Annotations? |
 * |---|---|---|
 * | Data (remote) | [com.example.cleanvault.data.remote.NoteDto] | No — but field names must match JSON |
 * | Data (local)  | [com.example.cleanvault.data.local.NoteEntity] | Yes — `@Entity`, `@PrimaryKey` from Room |
 * | Domain        | [Note] (this class) | **No** — pure Kotlin, zero framework dependencies |
 * | Presentation  | [Note] (same) | Shared with Domain for simplicity in this project |
 *
 * The Domain Model (`Note`) is the **single source of truth** for what a note
 * means in the context of the app's business rules. It knows nothing about
 * databases, networks, or UI frameworks.
 *
 * ---
 *
 * ## Why "Zero Android Dependencies"?
 *
 * Because the Domain Layer has no Android imports, it can be tested with
 * **plain JUnit** — no Android emulator or Robolectric required.
 * Your business logic tests run in milliseconds, not seconds.
 *
 * ---
 *
 * ## OOP Concept: Data Class & Immutability
 *
 * All properties are `val` (read-only). Once a [Note] is created, it cannot be
 * mutated. To "change" a note, you create a new one using `copy()`:
 * ```kotlin
 * val updated = note.copy(title = "New Title")
 * ```
 * Immutable objects are **thread-safe** — no synchronisation needed when passing
 * them across coroutines or threads.
 *
 * ---
 *
 * ## Encrypted vs Decrypted
 *
 * [isEncrypted] is a metadata flag. When `true`, it signals that the `content`
 * field contains an encrypted value that needs to be decrypted before display.
 * In the current architecture, truly sensitive secrets bypass this model entirely
 * and use [com.example.cleanvault.data.local.SecureStorageManager] directly.
 *
 * @property id          Unique identifier for the note
 * @property title       The title of the note shown in the list
 * @property content     The full content/body of the note
 * @property isEncrypted `true` if [content] is stored in encrypted form
 *
 * @see com.example.cleanvault.domain.repository.NoteRepository
 * @see com.example.cleanvault.domain.usecase.GetNotesUseCase
 */
data class Note(
    /** Unique ID of the note. Matches the `id` from the API and the Room database. */
    val id: Int,

    /** Title of the note, displayed prominently in the UI. */
    val title: String,

    /** Full body/content of the note. May be encrypted depending on [isEncrypted]. */
    val content: String,

    /**
     * Flag indicating whether this note's [content] is encrypted.
     * `true`  → content is ciphertext and should be decrypted before display.
     * `false` → content is plaintext and can be shown directly.
     */
    val isEncrypted: Boolean
)