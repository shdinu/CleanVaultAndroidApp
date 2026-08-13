package com.example.cleanvault

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * # CleanVaultApp — Application Entry Point
 *
 * This is the **very first class** that Android creates when the app starts.
 * It extends [Application], which is Android's global singleton class for the entire app process.
 *
 * ---
 *
 * ## What is `@HiltAndroidApp`?
 *
 * [HiltAndroidApp] is an annotation from **Dagger Hilt** — Google's recommended
 * Dependency Injection (DI) framework for Android.
 *
 * When you place `@HiltAndroidApp` on the [Application] class, Hilt:
 * 1. Generates the DI component tree at **compile time** (no runtime reflection overhead).
 * 2. Initialises all singleton-scoped dependencies (like the database, Retrofit, etc.)
 *    before any Activity or ViewModel starts.
 *
 * ---
 *
 * ## OOP Concept: Inheritance
 *
 * ```
 * CleanVaultApp  ←→  extends  ←→  android.app.Application
 * ```
 * By inheriting from [Application], this class gains access to the app-wide [Context],
 * which is required by Room (database), EncryptedSharedPreferences, and Hilt itself.
 *
 * ---
 *
 * ## Clean Architecture Role
 *
 * This class sits **outside** all three layers (Data / Domain / Presentation).
 * It is the bootstrap glue that lets Hilt wire everything together before the UI appears.
 *
 * ---
 *
 * ## Intern Tip
 *
 * You must declare this class in **AndroidManifest.xml** with:
 * ```xml
 * android:name=".CleanVaultApp"
 * ```
 * Otherwise Android will use the default [Application] class and Hilt will never initialise.
 *
 * @see dagger.hilt.android.HiltAndroidApp
 * @see android.app.Application
 */
@HiltAndroidApp
class CleanVaultApp : Application()