package com.example.cleanvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * # AppDatabase — Room Database Definition
 *
 * This abstract class is the **main access point** to the app's local SQLite database.
 * Room uses this class to know:
 * 1. Which tables (entities) exist in the database.
 * 2. What version the database schema is at (for migrations).
 * 3. Which DAO interfaces to expose.
 *
 * ---
 *
 * ## What is Room?
 *
 * Room is part of **Android Jetpack** and is an Object-Relational Mapping (ORM) library
 * built on top of SQLite. Instead of writing raw SQL like:
 * ```sql
 * CREATE TABLE notes (id INTEGER PRIMARY KEY, title TEXT, content TEXT, isEncrypted INTEGER)
 * SELECT * FROM notes
 * ```
 * …Room generates all of this from your Kotlin annotations at **compile time**.
 *
 * ---
 *
 * ## `@Database` Annotation Parameters
 *
 * | Parameter | Value | Meaning |
 * |---|---|---|
 * | `entities` | `[NoteEntity::class]` | The list of tables in this database |
 * | `version` | `1` | Current schema version — increment when you change the schema |
 * | `exportSchema` | `false` | Don't write schema JSON to disk (set to `true` in production for migrations) |
 *
 * ---
 *
 * ## OOP Concept: Abstraction
 *
 * [AppDatabase] is declared `abstract`. You never instantiate it with `AppDatabase()`.
 * Room's annotation processor generates a hidden concrete subclass at compile time
 * (e.g., `AppDatabase_Impl`). This is the **Abstract Factory** design pattern.
 *
 * ---
 *
 * ## Clean Architecture: Data Layer
 *
 * [AppDatabase] belongs to the **Data Layer**. It is created as a **singleton** in
 * [com.example.cleanvault.di.AppModule] and injected wherever local data access is needed.
 *
 * ---
 *
 * ## Multithreading with Room
 *
 * Room **enforces** that database queries do NOT run on the main (UI) thread.
 * When using `suspend` functions or `Flow` in the DAO, Room automatically switches
 * to a background thread. If you try to run a query on the main thread, Room throws
 * an `IllegalStateException` — protecting your app from ANR (App Not Responding) crashes.
 *
 * @see NoteEntity
 * @see NoteDao
 * @see com.example.cleanvault.di.AppModule.provideDatabase
 */
@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to the [NoteDao] — the interface for all note-related
     * SQL operations.
     *
     * Room generates the implementation of this abstract method at compile time.
     * You simply call `db.noteDao()` to get a usable DAO without writing
     * a single line of SQL connection code.
     *
     * @return A Room-generated implementation of [NoteDao]
     */
    abstract fun noteDao(): NoteDao
}