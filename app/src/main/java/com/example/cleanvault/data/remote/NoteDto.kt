package com.example.cleanvault.data.remote

/**
 * # NoteDto — Data Transfer Object (API Response Model)
 *
 * A **DTO (Data Transfer Object)** is a simple container class used to transfer
 * data between layers or systems. [NoteDto] represents the raw JSON structure
 * that comes back from the remote API — field names must match the JSON keys exactly
 * for Gson to deserialize them correctly.
 *
 * ---
 *
 * ## What is a DTO and Why Do We Need It?
 *
 * When you call a REST API, you receive raw JSON:
 * ```json
 * {
 *   "userId": 1,
 *   "id": 1,
 *   "title": "sunt aut facere repellat provident...",
 *   "body": "quia et suscipit\nsuscipit recusandae..."
 * }
 * ```
 *
 * Gson maps this JSON into the `data class` fields by matching **field names**.
 * So the JSON key `"title"` maps to the Kotlin property `title: String`.
 *
 * ---
 *
 * ## Why Separate from the Domain `Note` Model?
 *
 * This is a core **Clean Architecture** principle. The API returns a `body` field,
 * but our domain model calls it `content`. The API also returns `userId` which
 * we don't need in the UI.
 *
 * By keeping [NoteDto] separate:
 * - API changes only affect this file.
 * - Domain and UI code are insulated from API design decisions.
 * - We control exactly what data flows into our business logic.
 *
 * | Field | [NoteDto] (API) | [com.example.cleanvault.domain.model.Note] (Domain) |
 * |---|---|---|
 * | Unique ID | `id: Int` | `id: Int` |
 * | Title | `title: String` | `title: String` |
 * | Body | `body: String` | `content: String` (renamed!) |
 * | Encrypted? | ❌ not present | `isEncrypted: Boolean` |
 *
 * The conversion from [NoteDto] → [com.example.cleanvault.data.local.NoteEntity]
 * happens in [com.example.cleanvault.data.repository.NoteRepositoryImpl].
 *
 * ---
 *
 * ## OOP Concept: Data Class
 *
 * Kotlin `data class` auto-generates `equals()`, `hashCode()`, `toString()`, and `copy()`.
 * For a pure data holder like a DTO, this is ideal — no boilerplate needed.
 *
 * @property id    Unique identifier of the post/note from the API
 * @property title The title of the post
 * @property body  The body/content of the post (named `body` to match the JSON key)
 *
 * @see NoteApi
 * @see com.example.cleanvault.data.repository.NoteRepositoryImpl
 * @see com.example.cleanvault.domain.model.Note
 */
data class NoteDto(
    /** Unique post ID as returned by the jsonplaceholder API. */
    val id: Int,

    /** Post title. Mapped from JSON key `"title"`. */
    val title: String,

    /**
     * Post body/content. Mapped from JSON key `"body"`.
     * Note: This is renamed to `content` when converted to the domain [com.example.cleanvault.domain.model.Note].
     */
    val body: String
)