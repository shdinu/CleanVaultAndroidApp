package com.example.cleanvault.domain.usecase

import com.example.cleanvault.domain.model.Note
import com.example.cleanvault.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * # GetNotesUseCase — Business Logic: Retrieve Notes Stream
 *
 * A **Use Case** (also called an "Interactor") encapsulates a single piece of
 * **application business logic**. [GetNotesUseCase] represents the user's intention:
 * *"I want to observe the live stream of all notes."*
 *
 * ---
 *
 * ## What is a Use Case?
 *
 * In **Clean Architecture**, Use Cases are the business rules of the application.
 * They sit between the Presentation Layer (ViewModel) and the Data Layer (Repository).
 *
 * ```
 * ViewModel  →  calls  →  GetNotesUseCase  →  calls  →  NoteRepository
 * ```
 *
 * Why not call the repository directly from the ViewModel?
 * - As the app grows, a use case might filter notes by category, sort them,
 *   or merge data from multiple repositories.
 * - The ViewModel stays lean — it doesn't need to know HOW notes are retrieved.
 * - Use cases are easy to unit test in isolation.
 *
 * ---
 *
 * ## OOP Concept: `operator fun invoke()`
 *
 * By overloading the `invoke` operator, we can call this use case like a function:
 * ```kotlin
 * // Instead of: getNotesUseCase.execute()
 * // We can write:
 * val flow = getNotesUseCase()
 * ```
 * This gives use cases a clean, function-like interface — they are essentially
 * **callable objects** (similar to functors in C++ or `__call__` in Python).
 *
 * ---
 *
 * ## OOP Concept: Constructor Injection & Encapsulation
 *
 * `@Inject constructor` tells Hilt to provide the [NoteRepository] dependency.
 * The repository is `private` — external code cannot access or replace it.
 * This is **Encapsulation** in action.
 *
 * ---
 *
 * ## Clean Architecture: Domain Layer
 *
 * [GetNotesUseCase] depends **only on the [NoteRepository] interface** from the
 * same Domain Layer. It has zero dependencies on Android, Room, or Retrofit.
 * This makes it 100% unit-testable with plain JUnit and a mock repository.
 *
 * @param repository The [NoteRepository] interface injected by Hilt
 *
 * @see NoteRepository
 * @see Note
 * @see com.example.cleanvault.presentation.notes.NotesViewModel
 */
class GetNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {

    /**
     * Retrieves the live stream of all notes from the repository.
     *
     * By overloading `invoke`, this use case is called as:
     * ```kotlin
     * val notesFlow: Flow<List<Note>> = getNotesUseCase()
     * ```
     *
     * The returned [Flow] is a **cold reactive stream** that emits a new [List] of [Note]
     * objects every time the underlying data source (Room database) changes.
     *
     * The ViewModel subscribes to this [Flow] via `combine()` and `stateIn()`,
     * converting it into a [kotlinx.coroutines.flow.StateFlow] for the UI to observe.
     *
     * **Current behaviour:** Returns all notes without any filtering or sorting.
     * **Future extension:** Add parameters for filtering by category, sorting by date, etc.
     *
     * @return A [Flow] emitting an updated [List] of [Note] on every database change
     */
    operator fun invoke(): Flow<List<Note>> {
        return repository.getNotes()
    }
}