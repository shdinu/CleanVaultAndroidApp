package com.ambesoftnet.cleanvault.di

import android.content.Context
import androidx.room.Room
import com.ambesoftnet.cleanvault.data.local.AppDatabase
import com.ambesoftnet.cleanvault.data.local.NoteDao
import com.ambesoftnet.cleanvault.data.local.SecureStorageManager
import com.ambesoftnet.cleanvault.data.remote.NoteApi
import com.ambesoftnet.cleanvault.data.repository.NoteRepositoryImpl
import com.ambesoftnet.cleanvault.domain.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * # AppModule — Hilt Dependency Injection Module
 *
 * This is the **central wiring file** of the entire application.
 * Hilt reads this object at compile time and knows how to create and
 * provide every major dependency across all layers.
 *
 * ---
 *
 * ## What is Dependency Injection (DI)?
 *
 * Instead of creating dependencies inside a class (tight coupling):
 * ```kotlin
 * // BAD — ViewModel creates its own dependencies manually
 * class NotesViewModel {
 *     val api = Retrofit.Builder()...create(NoteApi::class.java)
 *     val db  = Room.databaseBuilder(...)
 * }
 * ```
 * DI inverts the responsibility — dependencies are *provided from outside*:
 * ```kotlin
 * // GOOD — ViewModel just declares what it needs; Hilt provides it
 * class NotesViewModel @Inject constructor(val useCase: GetNotesUseCase)
 * ```
 * This makes code **testable** (inject mocks in tests) and **maintainable**
 * (change implementation in ONE place — the module).
 *
 * ---
 *
 * ## Hilt Annotations
 *
 * | Annotation | Meaning |
 * |---|---|
 * | `@Module` | Marks this as a Hilt module — a source of dependencies |
 * | `@InstallIn(SingletonComponent::class)` | Dependencies live for the full app lifecycle |
 * | `@Provides` | This function creates (provides) a dependency |
 * | `@Singleton` | Only ONE instance is created; reused everywhere |
 *
 * ---
 *
 * ## OOP Concept: Dependency Inversion Principle (D in SOLID)
 *
 * Notice [provideRepository] returns the **interface** [NoteRepository],
 * not the concrete class [NoteRepositoryImpl]. This means:
 * - The rest of the app depends on the abstraction (interface).
 * - We can swap [NoteRepositoryImpl] with a FakeRepository in tests without
 *   changing ViewModel, UseCase, or UI code at all.
 *
 * ---
 *
 * ## Clean Architecture Role
 *
 * [AppModule] is the **only place** where all three layers (Data, Domain, Presentation)
 * are connected together. It's the composition root of the app.
 *
 * @see NoteApi
 * @see AppDatabase
 * @see SecureStorageManager
 * @see NoteRepository
 * @see NoteRepositoryImpl
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides a [CoroutineDispatcher] configured for I/O operations.
     *
     * ## What is a Dispatcher?
     *
     * In Kotlin Coroutines, a **dispatcher** decides which thread (or thread pool)
     * a coroutine runs on:
     * - [Dispatchers.IO] — optimised for disk/network I/O. Has a large thread pool (up to 64).
     * - [Dispatchers.Main] — the UI thread. Only one thread. Never block it!
     * - [Dispatchers.Default] — CPU-intensive computation (sorting, parsing).
     *
     * By injecting the dispatcher instead of hard-coding `Dispatchers.IO`,
     * we can replace it with a [kotlinx.coroutines.test.TestCoroutineDispatcher]
     * in unit tests for predictable, synchronous test execution.
     *
     * @return [Dispatchers.IO] — the standard background thread pool for I/O work
     */
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Provides the singleton [NoteApi] Retrofit instance.
     *
     * ## What is Retrofit?
     *
     * Retrofit is a type-safe HTTP client for Android. Instead of writing raw
     * `HttpURLConnection` code, you define a Kotlin interface ([NoteApi]) and
     * annotate methods with HTTP verbs (`@GET`, `@POST`, etc.).
     * Retrofit generates the implementation at runtime.
     *
     * ## Why Singleton?
     *
     * Creating a Retrofit instance is expensive — it parses annotations, sets up
     * thread pools, and allocates converters. One instance shared across the whole
     * app is efficient and correct.
     *
     * ## Gson Converter
     *
     * [GsonConverterFactory] tells Retrofit to use the Gson library to automatically
     * convert JSON strings → Kotlin data classes (and vice versa).
     * ```
     * {"id":1,"title":"Note","body":"Content"} → NoteDto(id=1, title="Note", body="Content")
     * ```
     *
     * @return A configured [NoteApi] Retrofit implementation
     */
    @Provides
    @Singleton
    fun provideNoteApi(): NoteApi {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoteApi::class.java)
    }

    /**
     * Provides the singleton [AppDatabase] Room database instance.
     *
     * ## What is Room?
     *
     * Room is an abstraction layer over Android's SQLite database.
     * Instead of writing raw SQL and `Cursor` code, you use:
     * - `@Entity` classes → database tables
     * - `@Dao` interfaces → SQL queries as Kotlin functions
     * - `@Database` abstract class → the database itself
     *
     * Room generates all the boilerplate SQL and threading code at compile time.
     *
     * ## `@ApplicationContext`
     *
     * Room needs the application [Context] to find the database file path on disk.
     * Hilt's `@ApplicationContext` qualifier tells Hilt to inject the app-level
     * context (not an Activity context which would leak memory).
     *
     * ## Why Singleton?
     *
     * A SQLite database file should only be opened by ONE connection at a time.
     * Having multiple [AppDatabase] instances would cause data corruption or
     * expensive re-initialisation. One singleton instance ensures correctness.
     *
     * @param context The application [Context] provided by Hilt automatically
     * @return A fully initialised [AppDatabase] instance
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "vault.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the [NoteDao] from the singleton [AppDatabase].
     *
     * ## What is a DAO?
     *
     * **Data Access Object** — an interface that defines all SQL operations
     * for a specific table. The DAO is how the repository layer talks to Room.
     *
     * Note: This provider is **not** `@Singleton` because [NoteDao] is just a
     * proxy/interface into the already-singleton database. Providing a new
     * DAO reference each time has zero cost.
     *
     * @param db The singleton [AppDatabase] instance
     * @return The [NoteDao] implementation generated by Room
     */
    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()

    /**
     * Provides the singleton [NoteRepository] implementation.
     *
     * ## Key Design Decision: Returns the Interface, Not the Concrete Class
     *
     * The return type is [NoteRepository] (interface from the **Domain layer**),
     * even though the actual object is [NoteRepositoryImpl] (from the **Data layer**).
     *
     * This enforces the **Dependency Inversion Principle**: all callers (ViewModel,
     * UseCases) depend on the abstraction, not the implementation.
     *
     * ## Multithreading
     *
     * The injected [CoroutineDispatcher] (`Dispatchers.IO`) is passed here so that
     * [NoteRepositoryImpl] can run all database and network operations on background
     * threads using `withContext(dispatcher)`.
     *
     * @param api          The Retrofit [NoteApi] for remote data fetching
     * @param dao          The Room [NoteDao] for local database operations
     * @param secureStorage The [SecureStorageManager] for AES-256 encrypted storage
     * @param dispatcher   The [CoroutineDispatcher] for background thread execution
     * @return A [NoteRepository] backed by [NoteRepositoryImpl]
     */
    @Provides
    @Singleton
    fun provideRepository(
        api: NoteApi,
        dao: NoteDao,
        secureStorage: SecureStorageManager,
        dispatcher: CoroutineDispatcher
    ): NoteRepository {
        return NoteRepositoryImpl(api, dao, secureStorage, dispatcher)
    }
}
