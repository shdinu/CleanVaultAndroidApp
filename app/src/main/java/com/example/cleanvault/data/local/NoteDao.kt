package com.example.cleanvault.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * # NoteDao — Data Access Object for Notes
 *
 * A **DAO (Data Access Object)** is a design pattern that provides an abstract interface
 * to a database. In Room, a DAO is a Kotlin `interface` where you declare your database
 * operations as functions, and Room generates the SQL and threading code for you.
 *
 * ---
 *
 * ## What is `@Dao`?
 *
 * The `@Dao` annotation marks this interface as a Room DAO. Room's annotation processor
 * reads this interface at **compile time** and generates a concrete implementation class
 * (e.g., `NoteDao_Impl`) that contains the actual SQLite code.
 *
 * As a developer, you only see and use this clean interface — Room hides all the
 * `ContentValues`, `Cursor`, and `SQLiteDatabase` boilerplate.
 *
 * ---
 *
 * ## OOP Concept: Interface & Abstraction
 *
 * [NoteDao] is an `interface` — a **pure contract** that says what operations are
 * available, without saying *how* they are implemented. This follows the **Interface
 * Segregation** and **Dependency Inversion** principles from SOLID.
 *
 * ---
 *
 * ## Clean Architecture: Data Layer
 *
 * [NoteDao] lives in the **Data Layer** (`data.local`). It knows about [NoteEntity]
 * (the database model) but knows nothing about the domain [com.example.cleanvault.domain.model.Note]
 * or the UI. The repository converts between these types.
 *
 * @see NoteEntity
 * @see AppDatabase
 * @see com.example.cleanvault.data.repository.NoteRepositoryImpl
 */
@Dao
interface NoteDao {

    /**
     * Returns a **reactive stream** of all notes from the `notes` table.
     *
     * ## Why `Flow<List<NoteEntity>>` Instead of `List<NoteEntity>`?
     *
     * A regular `List` is a **one-time snapshot** — you call the function,
     * get the current data, and that's it. If the database changes afterward,
     * you'd have to call the function again manually.
     *
     * A [Flow] is a **live data stream**. Room automatically emits a new
     * `List<NoteEntity>` every time any row in the `notes` table is
     * inserted, updated, or deleted. This drives automatic UI updates without
     * polling or manual refresh logic.
     *
     * ## Coroutines & Multithreading
     *
     * Notice this function is **not** `suspend`. That's because `Flow` is already
     * asynchronous — it doesn't block. Room runs the query on a background thread
     * and emits results via the Flow, which the ViewModel collects safely.
     *
     * ## SQL Query
     *
     * ```sql
     * SELECT * FROM notes
     * ```
     * Returns all columns of all rows from the `notes` table.
     *
     * @return A [Flow] that emits a new [List] of [NoteEntity] objects
     *         every time the `notes` table is modified
     *
     * @see kotlinx.coroutines.flow.Flow
     */
    @Query("SELECT * FROM notes")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /**
     * Inserts a list of [NoteEntity] objects into the `notes` table.
     *
     * ## `@Insert` Annotation
     *
     * Room generates the SQL `INSERT INTO notes (id, title, content, isEncrypted) VALUES (?, ?, ?, ?)`
     * statement for each entity in the list. You don't write any SQL.
     *
     * ## Conflict Strategy: REPLACE
     *
     * `OnConflictStrategy.REPLACE` means:
     * - If a row with the **same primary key** (`id`) already exists → **delete it and insert the new one**.
     * - If no conflict → insert normally.
     *
     * This is perfect for a sync use case where remote data should always override
     * stale local data.
     *
     * ## Coroutines: `suspend`
     *
     * `suspend` means this function is a **coroutine function**. It can be called
     * from a coroutine (e.g., inside `viewModelScope.launch {}` or `withContext(IO) {}`).
     * It **suspends** (pauses) the calling coroutine while the database write happens,
     * then resumes automatically — no callback hell, no `AsyncTask`.
     *
     * @param notes The list of [NoteEntity] objects to insert or replace
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)
}