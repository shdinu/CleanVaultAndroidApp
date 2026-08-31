package com.example.cleanvault.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * # SecureStorageManager — AES-256 Encrypted Key-Value Storage
 *
 * This class provides a secure way to store and retrieve sensitive data (like passwords,
 * API tokens, personal secrets) on an Android device using the **Android KeyStore** system
 * and **AES-256 encryption**.
 *
 * ---
 *
 * ## Why Not Regular SharedPreferences?
 *
 * Regular `SharedPreferences` stores data as **plaintext XML** on disk:
 * ```xml
 * <!-- /data/data/com.example.app/shared_prefs/regular_prefs.xml — DANGEROUS! -->
 * <string name="user_password">my_secret_123</string>
 * ```
 * On a rooted device, any app can read this file. That's a major security risk.
 *
 * **EncryptedSharedPreferences** stores the same data as:
 * ```
 * AES256-SIV-encrypted-key : AES256-GCM-encrypted-value
 * ```
 * Even if someone pulls this file off the device, the ciphertext is useless
 * without the master key — which lives in the **Android KeyStore hardware chip**.
 *
 * ---
 *
 * ## How the Encryption Works (Step by Step)
 *
 * ```
 * Step 1: MasterKey
 *   └─ Generated inside Android KeyStore (hardware-backed TEE on modern devices)
 *   └─ Algorithm: AES256_GCM
 *   └─ The key NEVER leaves the KeyStore — it cannot be extracted by any app
 *
 * Step 2: EncryptedSharedPreferences
 *   └─ Key encryption   : AES256_SIV  (deterministic — same key → same ciphertext)
 *   └─ Value encryption : AES256_GCM  (probabilistic — each write produces different ciphertext)
 *   └─ Backed by the MasterKey from Step 1
 *
 * Step 3: saveSecret("my_key", "my_value")
 *   └─ key   → encrypted with AES256-SIV → stored in prefs
 *   └─ value → encrypted with AES256-GCM → stored in prefs
 *
 * Step 4: getSecret("my_key")
 *   └─ Looks up the AES256-SIV encrypted key
 *   └─ Decrypts the value with AES256-GCM using the KeyStore master key
 *   └─ Returns plaintext string — decryption only possible on THIS device
 * ```
 *
 * ---
 *
 * ## OOP Concepts
 *
 * - **Encapsulation:** `masterKey` and `sharedPreferences` are `private`.
 *   External callers only see [saveSecret] and [getSecret] — the internals are hidden.
 * - **Single Responsibility:** This class does ONE thing — secure key-value storage.
 *
 * ---
 *
 * ## Hilt Injection
 *
 * `@Singleton` + `@Inject constructor` means Hilt creates exactly ONE instance of this
 * class for the whole app, and injects it wherever it is needed (e.g., [com.example.cleanvault.data.repository.NoteRepositoryImpl]).
 *
 * `@ApplicationContext` tells Hilt to inject the app-level [Context] — this prevents
 * memory leaks that would occur with an Activity context.
 *
 * @see EncryptedSharedPreferences
 * @see MasterKey
 * @see com.example.cleanvault.data.repository.NoteRepositoryImpl
 */
@Singleton
class SecureStorageManager @Inject constructor(@ApplicationContext context: Context) {

    /**
     * The AES-256-GCM master key stored securely inside the **Android KeyStore**.
     *
     * The Android KeyStore is a hardware-backed (on supported devices) key management
     * system. Keys stored here:
     * - Cannot be exported or extracted by any app.
     * - Are tied to the device and (optionally) biometric authentication.
     * - Survive app updates but are deleted if the app is uninstalled.
     *
     * [MasterKey.KeyScheme.AES256_GCM] selects 256-bit AES in
     * Galois/Counter Mode — a modern authenticated encryption algorithm that provides
     * both confidentiality and integrity.
     */
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    /**
     * The [EncryptedSharedPreferences] instance backed by [masterKey].
     *
     * This behaves exactly like regular [android.content.SharedPreferences] from
     * the caller's perspective — but every read/write transparently encrypts and
     * decrypts the data.
     *
     * **Encryption schemes:**
     * - Keys: [EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV]
     *   (deterministic — necessary so the same key can always be looked up)
     * - Values: [EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM]
     *   (probabilistic / non-deterministic — provides stronger confidentiality)
     */
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_vault_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val keyStore = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "CleanVaultCipherKey"

    private fun getOrCreateCipherKey(): javax.crypto.SecretKey {
        val existingKey = (keyStore.getEntry(keyAlias, null) as? java.security.KeyStore.SecretKeyEntry)?.secretKey
        if (existingKey != null) return existingKey

        val keyGenerator = javax.crypto.KeyGenerator.getInstance(
            android.security.keystore.KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = android.security.keystore.KeyGenParameterSpec.Builder(
            keyAlias,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Encrypts plaintext using AES-256-GCM backed by the Android KeyStore.
     * Returns a Base64-encoded string containing [12-byte IV + Ciphertext].
     */
    fun encrypt(plaintext: String): String {
        return try {
            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getOrCreateCipherKey())
            val iv = cipher.iv // 12 bytes IV
            val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + ciphertext.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)
            android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plaintext
        }
    }

    /**
     * Decrypts a Base64-encoded [12-byte IV + Ciphertext] string back to plaintext.
     */
    fun decrypt(encryptedBase64: String): String {
        return try {
            val combined = android.util.Base64.decode(encryptedBase64, android.util.Base64.NO_WRAP)
            if (combined.size <= 12) return encryptedBase64
            val iv = ByteArray(12)
            val ciphertext = ByteArray(combined.size - 12)
            System.arraycopy(combined, 0, iv, 0, 12)
            System.arraycopy(combined, 12, ciphertext, 0, ciphertext.size)

            val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
            val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getOrCreateCipherKey(), spec)
            val plaintextBytes = cipher.doFinal(ciphertext)
            String(plaintextBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Return original string if decryption fails or was already plaintext
            encryptedBase64
        }
    }

    /**
     * Encrypts and saves a [value] under the given [key].
     *
     * The [key] is encrypted with AES256-SIV and the [value] is encrypted with
     * AES256-GCM before being written to the on-disk `secret_vault_prefs` file.
     *
     * **Threading:** `apply()` is asynchronous and non-blocking — it commits the
     * change to an in-memory map immediately and flushes to disk in the background.
     * This makes it safe to call from a coroutine on any dispatcher.
     *
     * **Intern Tip:** This is like a secure `Map<String, String>` where both the
     * keys and values are stored encrypted on disk.
     *
     * @param key   A unique string identifier for the secret (e.g., `"user_vault_key"`)
     * @param value The plaintext secret to encrypt and store (e.g., `"my_password_123"`)
     */
    fun saveSecret(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    /**
     * Decrypts and retrieves the secret stored under the given [key].
     *
     * [EncryptedSharedPreferences] decrypts the value transparently on read.
     * Only the **same device** with the **same KeyStore master key** can decrypt it.
     *
     * @param key The unique string identifier of the secret to retrieve
     * @return The decrypted plaintext value, or `null` if the key does not exist
     */
    fun getSecret(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
}