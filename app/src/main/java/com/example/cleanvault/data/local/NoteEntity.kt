package com.example.cleanvault.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * # NoteEntity — Room Database Table Definition
 *
 * This `data class` represents a **single row** in the `notes` SQLite table.
 * Room reads the annotations on this class at compile time and generates the
 * `CREATE TABLE` SQL statement automatically.
 *
 * ---
 *
 * ## `@Entity` — Declaring a Database Table
 *
 * The `@Entity` annotation marks this class as a Room table.
 * - `tableName = "notes"` → the table will be named `notes` in SQLite.
 * - Each **field** of the `data class` becomes a **column** in that table.
 *
 * **Generated SQL (conceptual):**
 * ```sql
 * CREATE TABLE IF NOT EXISTS `notes` (
 *   `id`          INTEGER NOT NULL,
 *   `title`       TEXT    NOT NULL,
 *   `content`     TEXT    NOT NULL,
 *   `isEncrypted` INTEGER NOT NULL,
 *   PRIMARY KEY(`id`)
 * )
 * ```
 *
 * ---
 *
 * ## OOP Concept: Data Class
 *
 * Kotlin `data class` automatically generates:
 * - `equals()` / `hashCode()` — compare two notes by their field values
 * - `toString()` — human-readable representation
 * - `copy()` — create a copy with some fields changed
 *
 * This is far less boilerplate than writing the same thing in Java.
 *
 * ---
 *
 * ## Why Is This Separate from `Note.kt` (Domain Model)?
 *
 * This is a key **Clean Architecture** principle called **layer separation**:
 *
 * | Model | Layer | Purpose |
 * |---|---|---|
 * | [NoteEntity] | Data Layer | Represents a database row. Has Room annotations. |
 * | [com.example.cleanvault.data.remote.NoteDto] | Data Layer | Represents the JSON API response. |
 * | [com.example.cleanvault.domain.model.Note] | Domain Layer | Pure business model. No framework annotations. |
 *
 * If the API changes its JSON schema, only `NoteDto` changes.
 * If the database schema changes, only `NoteEntity` changes.
 * The domain `Note` (and all business logic) stays untouched.
 *
 * ---
 *
 * ## Encrypted Storage Note
 *
 * The `isEncrypted` flag indicates whether the `content` field was encrypted
 * before being stored. In the current implementation, notes fetched from the
 * remote API are stored with `isEncrypted = false` (plaintext). Truly sensitive
 * secrets use a separate encrypted storage mechanism ([SecureStorageManager]).
 *
 * @property id          The unique identifier for this note (maps to the SQL `PRIMARY KEY`)
 * @property title       The title of the note
 * @property content     The body/content of the note
 * @property isEncrypted Whether the content is stored in encrypted form
 *
 * @see NoteDao
 * @see AppDatabase
 * @see com.example.cleanvault.domain.model.Note
 */
@Entity(tableName = "notes")
data class NoteEntity(
    /** Unique note ID. Acts as the primary key in the SQLite `notes` table. */
    @PrimaryKey val id: Int,

    /** The note's title — stored as a TEXT column. */
    val title: String,

    /** The note's content/body — stored as a TEXT column. */
    val content: String,

    /**
     * Indicates if this note's content is encrypted.
     * Stored as INTEGER in SQLite (0 = false, 1 = true).
     * Room handles the Boolean ↔ INTEGER conversion automatically.
     */
    val isEncrypted: Boolean
)