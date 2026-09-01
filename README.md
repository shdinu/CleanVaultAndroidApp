# 🔐 CleanVault – Secure Notes Android App

> A production-grade Android demo application that teaches **Clean Architecture**, **MVVM**, **Jetpack Compose**, **Coroutines**, **Retrofit**, **Room DB**, and **Encrypted Storage** — all in one project.
>
> **Perfect for interns and junior developers** who want to understand how a real-world Android app is structured and why every design decision is made.

---

## 📖 Table of Contents

1. [What Does This App Do?](#what-does-this-app-do)
2. [Tech Stack at a Glance](#tech-stack-at-a-glance)
3. [Project Architecture – Clean Architecture Explained](#project-architecture--clean-architecture-explained)
4. [Package Structure](#package-structure)
5. [Layer-by-Layer Walkthrough](#layer-by-layer-walkthrough)
   - [Data Layer](#1-data-layer)
   - [Domain Layer](#2-domain-layer)
   - [Presentation Layer](#3-presentation-layer)
   - [Dependency Injection (DI)](#4-dependency-injection-di)
6. [Key Android Concepts Demonstrated](#key-android-concepts-demonstrated)
   - [MVVM Architecture](#mvvm-architecture)
   - [Jetpack Compose](#jetpack-compose)
   - [Coroutines & Multithreading](#coroutines--multithreading)
   - [Retrofit – REST API](#retrofit--rest-api)
   - [Room Database](#room-database)
   - [Encrypted Data Storage](#encrypted-data-storage)
   - [OOP Concepts](#oop-concepts)
7. [Data Flow Diagram](#data-flow-diagram)
8. [How to Build & Run](#how-to-build--run)
9. [Dependencies](#dependencies)
10. [Frequently Asked Questions (Intern Edition)](#frequently-asked-questions-intern-edition)
11. [☕ Support / Buy Me a Coffee](#-support--buy-me-a-coffee)

---

## What Does This App Do?

**CleanVault** is a **secure notes application** that:

| Feature | Description |
|---|---|
| 📥 Fetches notes from a remote API | Uses **Retrofit** to call `jsonplaceholder.typicode.com` |
| 💾 Stores notes locally | Uses **Room Database** (SQLite wrapper) |
| 🔒 Saves secrets securely | Uses **EncryptedSharedPreferences** with Android **KeyStore** (AES-256) |
| 🔓 Decrypts and displays secrets | Decrypts on-the-fly and shows the plaintext value in the UI |
| 🎨 Modern UI | Built with **Jetpack Compose** + **Material 3** |
| 🧵 Non-blocking operations | All I/O runs on background threads using **Coroutines** |
| 💉 Dependency Injection | Managed by **Hilt** (Google's DI framework) |

---

## Tech Stack at a Glance

```
Language         : Kotlin
Architecture     : Clean Architecture + MVVM
UI               : Jetpack Compose (Material 3)
Async            : Kotlin Coroutines + StateFlow
Networking       : Retrofit 2 + Gson
Local DB         : Room (SQLite)
Encryption       : AndroidX Security Crypto (EncryptedSharedPreferences + AES-256-GCM)
DI               : Dagger Hilt
Min SDK          : 26 (Android 8.0)
Target SDK       : 34 (Android 14)
Build System     : Gradle (Kotlin DSL)
```

---

## Project Architecture – Clean Architecture Explained

> **Clean Architecture** (by Uncle Bob) splits code into isolated layers where **inner layers never know about outer layers**. This makes the code testable, maintainable, and scalable.

```
┌─────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                    │
│   (Compose UI, ViewModel, UiState)                      │
│   → Knows about: Domain Layer only                      │
├─────────────────────────────────────────────────────────┤
│                     DOMAIN LAYER                        │
│   (Business Logic: UseCases, Repository Interface,      │
│    Domain Models)                                       │
│   → Pure Kotlin. Zero Android dependencies!             │
├─────────────────────────────────────────────────────────┤
│                      DATA LAYER                         │
│   (Repository Impl, Room DB, Retrofit API, Encryption)  │
│   → Implements Domain interfaces                        │
└─────────────────────────────────────────────────────────┘
```

### Why three layers?

- **Separation of Concerns** – Each layer has one responsibility.
- **Testability** – You can unit-test the Domain layer without touching the database or network.
- **Replaceability** – Swap Room for a different DB without changing ViewModel code.
- **Scalability** – Add new features in one layer without breaking others.

---

## Package Structure

```
com.ambesoftnet.cleanvault/
│
├── CleanVaultApp.kt                    ← Application entry point (@HiltAndroidApp)
├── MainActivity.kt                     ← Single Activity host for Compose
│
├── di/
│   └── AppModule.kt                    ← Hilt DI: provides all singleton dependencies
│
├── data/                               ← DATA LAYER
│   ├── local/
│   │   ├── AppDatabase.kt              ← Room database definition
│   │   ├── NoteDao.kt                  ← Room DAO (Data Access Object)
│   │   ├── NoteEntity.kt               ← Room table entity (maps to DB columns)
│   │   └── SecureStorageManager.kt     ← AES-256 encrypted storage via Android KeyStore
│   ├── remote/
│   │   ├── NoteApi.kt                  ← Retrofit API interface
│   │   └── NoteDto.kt                  ← Data Transfer Object (API response model)
│   └── repository/
│       └── NoteRepositoryImpl.kt       ← Concrete implementation of NoteRepository
│
├── domain/                             ← DOMAIN LAYER (pure Kotlin, no Android)
│   ├── model/
│   │   └── Note.kt                     ← Domain model (business entity)
│   ├── repository/
│   │   └── NoteRepository.kt           ← Abstract contract (interface)
│   └── usecase/
│       ├── GetNotesUseCase.kt          ← Business rule: get all notes as a stream
│       └── SaveSecretNoteUseCase.kt    ← Business rule: validate & save encrypted secret
│
└── presentation/                       ← PRESENTATION LAYER
    └── notes/
        ├── NotesScreen.kt              ← Jetpack Compose UI
        ├── NotesUiState.kt             ← UI state model (sealed interface)
        └── NotesViewModel.kt           ← ViewModel: connects Domain ↔ UI
```

---

## Layer-by-Layer Walkthrough

### 1. Data Layer

The **Data Layer** is responsible for all data operations — fetching from the internet, saving to a local database, and storing secrets securely.

#### `NoteApi.kt` – Retrofit Interface
```kotlin
interface NoteApi {
    @GET("posts")
    suspend fun getRemoteNotes(): List<NoteDto>
}
```
> **Intern Tip:** `suspend` means "this function can pause without blocking the UI thread." Retrofit automatically runs network calls on a background thread when you use `suspend`.

#### `NoteDto.kt` – Data Transfer Object
This is the raw JSON model from the server. It's intentionally separate from the domain `Note` model. Why? Because the API might change, or return extra fields you don't need — you don't want that leaking into your business logic.

#### `AppDatabase.kt` – Room Database
```kotlin
@Database(entities = [NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
```
> **Intern Tip:** Room is an abstraction over SQLite. `@Database` tells Room what tables exist. You never write `CREATE TABLE` SQL by hand — Room generates it.

#### `NoteEntity.kt` – Database Table
```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val content: String,
    val isEncrypted: Boolean
)
```
> Each field becomes a **column** in the SQLite `notes` table.

#### `SecureStorageManager.kt` – AES-256 Encrypted Storage
This class uses the **Android KeyStore** to generate a master key and then stores key-value pairs in `EncryptedSharedPreferences`.
- **AES256_GCM** encrypts the *values*
- **AES256_SIV** encrypts the *keys*
- The master key lives in Android's hardware-backed security chip (TEE) — it never leaves the device in plaintext.

#### `NoteRepositoryImpl.kt` – The Bridge
This is where **Data Layer** meets the **Domain Layer**. It:
1. Fetches notes from Retrofit → converts `NoteDto` → `NoteEntity` → saves to Room.
2. Exposes Room notes as a `Flow<List<Note>>` (domain model).
3. Delegates encryption/decryption to `SecureStorageManager`.
4. All heavy I/O uses `withContext(ioDispatcher)` to ensure operations run on a **background thread**.

---

### 2. Domain Layer

The **Domain Layer** contains your app's **business rules** — and critically, **it has zero Android imports**. This makes it 100% unit-testable with plain JVM.

#### `Note.kt` – Domain Model
```kotlin
data class Note(val id: Int, val title: String, val content: String, val isEncrypted: Boolean)
```
> The domain model is what your ViewModel and UI work with. It's a clean, simple data class — not polluted by Room annotations or JSON field names.

#### `NoteRepository.kt` – The Contract (Interface)
```kotlin
interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun fetchAndSyncNotes()
    suspend fun saveEncryptedSecret(key: String, secret: String)
    suspend fun getDecryptedSecret(key: String): String?
}
```
> **OOP Concept: Abstraction.** The Domain layer only knows *what* the repository can do, not *how* it does it. The concrete implementation lives in the Data layer. This is also the **Dependency Inversion Principle (D in SOLID)**.

#### `GetNotesUseCase.kt` – Single-Responsibility Business Logic
```kotlin
class GetNotesUseCase @Inject constructor(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getNotes()
}
```
> **Why have a UseCase if it's just one line?** As the app grows, you'd add filtering, sorting, or business validation here. The ViewModel stays clean and delegates to the UseCase.

#### `SaveSecretNoteUseCase.kt` – Validated Save
```kotlin
suspend operator fun invoke(key: String, secret: String) {
    require(secret.isNotBlank()) { "Secret cannot be blank" }
    repository.saveEncryptedSecret(key, secret)
}
```
> Notice the **business rule**: a blank secret is rejected. This logic lives in the Domain layer — not the ViewModel, not the UI.

---

### 3. Presentation Layer

The **Presentation Layer** is what the user sees. It follows **MVVM (Model-View-ViewModel)** pattern.

#### `NotesUiState.kt` – Sealed Interface for UI States
```kotlin
sealed interface NotesUiState {
    object Loading : NotesUiState
    data class Success(val notes: List<Note>, val decryptedSecret: String?) : NotesUiState
    data class Error(val message: String) : NotesUiState
}
```
> **OOP: Sealed Interface** — Like an enum, but each variant can carry its own data. The UI uses `when(state) { ... }` to render different content for Loading, Success, and Error — a safe, exhaustive pattern.

#### `NotesViewModel.kt` – The Middleman
```kotlin
val uiState: StateFlow<NotesUiState> = combine(
    getNotesUseCase(),
    _decryptedSecret
) { notes, secret ->
    NotesUiState.Success(notes = notes, decryptedSecret = secret)
}.catch {
    emit(NotesUiState.Error(it.message ?: "An unexpected error occurred"))
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = NotesUiState.Loading
)
```
> **Key Concepts:**
> - `StateFlow` – A hot stream that always holds the latest value.
> - `combine()` – Merges two flows into one.
> - `.catch {}` – Safe error handling.
> - `SharingStarted.WhileSubscribed(5000)` – Saves battery when app is backgrounded.

#### `NotesScreen.kt` – Jetpack Compose UI
```kotlin
val uiState by viewModel.uiState.collectAsState()
```
> Compose **reactively re-renders** every time `uiState` changes. You never manually call `notifyDataSetChanged()` or update TextViews.

---

### 4. Dependency Injection (DI)

#### `AppModule.kt` – Hilt Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideNoteApi(): NoteApi { ... }

    @Provides @Singleton
    fun provideDatabase(...): AppDatabase { ... }

    @Provides @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

> **Why Singleton?** Creating a new database or network client every time is expensive. Singletons are created once and reused everywhere.

---

## Key Android Concepts Demonstrated

### MVVM Architecture

```
View (NotesScreen.kt)
  ↕  collectAsState() / events
ViewModel (NotesViewModel.kt)
  ↕  UseCase calls
Domain (UseCase + Repository Interface)
  ↕  Implementation
Data (Repository + Room + Retrofit)
```

---

### Jetpack Compose

- `@Composable` = this function draws UI
- `remember {}` = store state that survives recompositions
- `LazyColumn` = RecyclerView equivalent in Compose
- `Scaffold`, `TopAppBar`, `Card`, `Button` = pre-built Material 3 components

---

### Coroutines & Multithreading

| Concept | Where Used | What It Does |
|---|---|---|
| `suspend fun` | `NoteDao`, `NoteApi`, `NoteRepositoryImpl` | Marks a function as asynchronous |
| `withContext(ioDispatcher)` | `NoteRepositoryImpl` | Switches to a background thread for I/O |
| `viewModelScope.launch` | `NotesViewModel` | Launches a coroutine tied to ViewModel lifecycle |
| `Flow<T>` | `NoteDao`, `NoteRepository` | A cold stream of data that emits over time |
| `StateFlow<T>` | `NotesViewModel.uiState` | A hot stream that always has a current value |

---

### Retrofit – REST API

```kotlin
Retrofit.Builder()
    .baseUrl("https://jsonplaceholder.typicode.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(NoteApi::class.java)
```

---

### Room Database

```
[NoteEntity] → @Entity → creates SQL table "notes"
[NoteDao]    → @Dao    → provides SQL queries as Kotlin functions
[AppDatabase]→ @Database → the database itself, holds DAOs
```

---

### Encrypted Data Storage

```
User types secret → ViewModel → SaveSecretNoteUseCase
    → SecureStorageManager.saveSecret()
    → EncryptedSharedPreferences (AES-256-GCM)
    → Encrypted bytes written to disk

To read:
    SecureStorageManager.getSecret()
    → decrypts using KeyStore master key
    → Plaintext returned to UI via StateFlow
```

---

### OOP Concepts

| Concept | Where Demonstrated |
|---|---|
| **Abstraction** | `NoteRepository` interface |
| **Encapsulation** | `SecureStorageManager` private fields |
| **Inheritance** | `AppDatabase : RoomDatabase()`, `CleanVaultApp : Application()` |
| **Polymorphism** | `NotesUiState` sealed interface |
| **Dependency Inversion** | ViewModel depends on interface, not concrete class |
| **Single Responsibility** | Each class has one job |
| **Open/Closed** | Add new UseCases without modifying existing ones |

---

## Data Flow Diagram

```
                     ┌──────────────────────┐
                     │    NotesScreen.kt     │
                     │   (Jetpack Compose)   │
                     └────────┬─────────────┘
                 collectAsState() │ user events
                     ┌────────▼─────────────┐
                     │  NotesViewModel.kt    │
                     │  (StateFlow, Hilt)    │
                     └──┬─────────┬──────────┘
           GetNotesUseCase  SaveSecretNoteUseCase
                     ┌──▼─────────▼──────────┐
                     │   NoteRepository.kt    │
                     │   (interface/contract) │
                     └──┬─────────┬──────────┘
                        │         │
          ┌─────────────▼─┐   ┌───▼──────────────────┐
          │  NoteApi.kt   │   │ SecureStorageManager  │
          │  (Retrofit)   │   │ (AES-256 KeyStore)    │
          └───────┬───────┘   └───────────────────────┘
                  │ NoteDto → NoteEntity
          ┌───────▼───────┐
          │   NoteDao.kt  │
          │  (Room/SQLite)│
          └───────┬───────┘
                  │ Flow<List<NoteEntity>> → Flow<List<Note>>
          ┌───────▼───────────────┐
          │  NotesUiState.Success │
          │  (emitted to UI)      │
          └───────────────────────┘
```

---

## How to Build & Run

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17**
- **Android SDK 34**
- Internet connection

### Steps

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd CleanVaultAndroidApp

# 2. Open in Android Studio
# File → Open → select the CleanVaultAndroidApp folder

# 3. Let Gradle sync

# 4. Connect device (API 26+) or start Emulator

# 5. Click the ▶ Run button
```

---

## Dependencies

| Library | Purpose |
|---|---|
| Jetpack Compose BOM | Compose UI framework |
| Material 3 | UI components |
| Hilt | Dependency Injection |
| Retrofit 2 | HTTP networking |
| Gson Converter | JSON parsing |
| Room | SQLite ORM |
| Room KTX | Coroutine support for Room |
| AndroidX Security Crypto | AES-256 encrypted storage |
| Lifecycle ViewModel Compose | ViewModel in Compose |
| Kotlin Coroutines | Async/background threading |

---

## Frequently Asked Questions (Intern Edition)

**Q: Why do we have `Note.kt` AND `NoteEntity.kt` AND `NoteDto.kt`?**
> - `NoteDto` = raw API response model.
> - `NoteEntity` = database row model (Room annotations).
> - `Note` = pure business model (no framework dependencies).
> Keeping them separate isolates changes to one layer without breaking others.

**Q: What is `Flow` and why not just return a `List`?**
> A `Flow` is a **stream of data over time**. Room's `getAllNotes()` returns a `Flow` so the UI **automatically updates** whenever the database changes.

**Q: Why `StateFlow` instead of `LiveData`?**
> `StateFlow` is Kotlin-native, works seamlessly with Coroutines, and integrates perfectly with `collectAsState()` in Compose.

**Q: What is Hilt and why do we need it?**
> Hilt eliminates manual dependency creation boilerplate. It creates and injects dependencies automatically and makes testing easy (swap implementations with fakes).

**Q: What does `@Singleton` mean?**
> The annotated object is created **once** for the entire lifetime of the app.

**Q: Is the secret truly safe with `EncryptedSharedPreferences`?**
> Yes. The master key is stored in the **Android KeyStore** (hardware-backed on modern devices). Even if someone extracts the prefs file, they cannot decrypt it without the hardware key.

---

## Architecture Summary

```
┌────────────────────────────────────────────────────────────────┐
│  CLEAN VAULT – Architecture at a Glance                        │
│                                                                │
│  Presentation  │  ViewModel + StateFlow + Compose              │
│  Domain        │  UseCases + Repository Interface + Models     │
│  Data          │  Retrofit + Room + EncryptedSharedPreferences │
│  DI            │  Hilt (Singleton scoped dependencies)         │
│  Threading     │  Coroutines (IO dispatcher + viewModelScope)  │
│  Security      │  AES-256-GCM via Android KeyStore             │
└────────────────────────────────────────────────────────────────┘
```

---

## ☕ Support / Buy Me a Coffee

If you found this project helpful or learned something new, consider buying me a coffee to support my work!

<div align="center">

<img src="https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=upi%3A%2F%2Fpay%3Fpa%3Dsh.dinu%40okicici%26pn%3DCleanVault%26cu%3DINR" alt="UPI QR Code - Buy Me A Coffee" width="200" height="200" />

<br/>

**Scan with any UPI App**  
*(Google Pay, PhonePe, Paytm, BHIM, Cred, etc.)*

```
UPI ID: sh.dinu@okicici
```

</div>

---

*Built with ❤️ to demonstrate modern Android development best practices.*
