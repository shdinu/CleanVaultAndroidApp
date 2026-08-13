package com.example.cleanvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.cleanvault.presentation.notes.NotesScreen
import com.example.cleanvault.presentation.notes.NotesViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * # MainActivity — Single Activity Entry Point
 *
 * This is the **only Activity** in this app. In modern Android development using
 * Jetpack Compose, it is best practice to have a single Activity that acts as a
 * "host" container for all Compose screens.
 *
 * ---
 *
 * ## What is an Activity?
 *
 * An [android.app.Activity] (or its modern extension [ComponentActivity]) represents
 * one "screen" or "window" in Android. It has a lifecycle:
 * ```
 * onCreate → onStart → onResume  (visible & interactive)
 *        ↓
 * onPause → onStop → onDestroy  (hidden or closed)
 * ```
 * We override [onCreate] to set up the UI when the screen is first created.
 *
 * ---
 *
 * ## What is `@AndroidEntryPoint`?
 *
 * [AndroidEntryPoint] is a Hilt annotation that tells Hilt:
 * *"This Activity wants dependencies injected into it."*
 *
 * Without this annotation, Hilt cannot inject dependencies (like ViewModels)
 * into this Activity.
 *
 * ---
 *
 * ## MVVM Architecture: ViewModel Delegation
 *
 * ```kotlin
 * private val viewModel: NotesViewModel by viewModels()
 * ```
 *
 * `by viewModels()` is a Kotlin **property delegate**. It:
 * - Creates the [NotesViewModel] the first time it's accessed.
 * - **Survives configuration changes** (e.g., screen rotation). The ViewModel is NOT
 *   re-created when the Activity is re-created — so your data is preserved.
 * - Automatically destroys the ViewModel when the Activity is permanently finished.
 *
 * ---
 *
 * ## Jetpack Compose: setContent
 *
 * ```kotlin
 * setContent {
 *     NotesScreen(viewModel = viewModel)
 * }
 * ```
 *
 * [setContent] replaces the traditional `setContentView(R.layout.activity_main)`.
 * Instead of inflating an XML layout, it starts a **Compose tree** — a hierarchy of
 * `@Composable` functions that describe what the screen should look like.
 *
 * ---
 *
 * ## OOP Concept: Inheritance & Single Responsibility
 *
 * [MainActivity] inherits from [ComponentActivity] (which in turn inherits from Activity).
 * It has ONE responsibility: boot the Compose UI. All business logic lives in the ViewModel.
 *
 * @see NotesViewModel
 * @see NotesScreen
 * @see dagger.hilt.android.AndroidEntryPoint
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * The [NotesViewModel] instance for this screen.
     *
     * Delegated via `by viewModels()` which uses Hilt to inject dependencies
     * into the ViewModel's constructor automatically.
     *
     * **Multithreading note:** ViewModels own a [kotlinx.coroutines.CoroutineScope]
     * (`viewModelScope`) which is automatically cancelled when the ViewModel is destroyed,
     * preventing coroutine/memory leaks.
     */
    private val viewModel: NotesViewModel by viewModels()

    /**
     * Called when the Activity is first created.
     *
     * This is the starting point of the UI. We call [setContent] here to launch
     * our Jetpack Compose UI tree rooted at [NotesScreen].
     *
     * @param savedInstanceState Bundle containing any previously saved state
     *        (e.g., after the system killed the app in background). Passed to super for framework use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesScreen(viewModel = viewModel)
        }
    }
}