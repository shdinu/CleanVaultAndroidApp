package com.example.cleanvault.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * # NoteDao — Data Access Object for Notes
 *
 * A **DAO (Data Access Object)** is a design pattern that provides an abstract interface
 * to a database. In Room, a DAO is a Kotlin `interface` where you declare your database
 * operations as functions, and Room generates the SQL and threading code for you.
 */
@Dao
interface NoteDao {

    /**
     * Returns a **reactive stream** of all notes from the `notes` table ordered by ID descending.
     */
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    /**
     * Retrieves a single note by its ID.
     */
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): NoteEntity?

    /**
     * Inserts a single [NoteEntity] into the `notes` table.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    /**
     * Inserts a list of [NoteEntity] objects into the `notes` table.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    /**
     * Updates an existing [NoteEntity] in the `notes` table.
     */
    @Update
    suspend fun updateNote(note: NoteEntity)

    /**
     * Deletes a [NoteEntity] from the `notes` table.
     */
    @Delete
    suspend fun deleteNote(note: NoteEntity)

    /**
     * Deletes a note by its unique ID.
     */
    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    /**
     * Deletes all notes from the database.
     */
    @Query("DELETE FROM notes")
    suspend fun clearAllNotes()
}