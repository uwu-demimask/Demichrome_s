package com.example.data.account

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.UserProfile
import com.example.data.UserProfileDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

/**
 * Represents the authenticated or local active user account in Demichrome.
 */
data class UserAccount(
    val userId: String,
    val email: String,
    val displayName: String,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = 0L,
    val photoUrl: String = ""
) {
    val isGuest: Boolean
        get() = isAnonymous || email.endsWith("@demichrome.local")
}

/**
 * Manages user authentication, persistent profiles, and account sessions.
 * Operates offline-first using Room database and secure credential hashing.
 */
class AccountManager(
    private val context: Context,
    private val userProfileDao: UserProfileDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val authPrefs = context.getSharedPreferences("demichrome_secure_auth", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_USER_ID = "active_user_id"
        private const val PREFIX_USER_PASS = "auth_pwd_hash_"
        private const val PREFIX_USER_SALT = "auth_salt_"

        val DEFAULT_LOCAL_USER = UserAccount(
            userId = "local_guest_master",
            email = "user@demichrome.app",
            displayName = "Demichrome User",
            isAnonymous = true,
            createdAt = System.currentTimeMillis(),
            lastSyncedAt = 0L
        )

        @Volatile
        private var INSTANCE: AccountManager? = null

        fun getInstance(context: Context): AccountManager {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = AccountManager(context.applicationContext, db.userProfileDao())
                INSTANCE = instance
                instance
            }
        }
    }

    private val _currentUser = MutableStateFlow(DEFAULT_LOCAL_USER)
    val currentUser: StateFlow<UserAccount> = _currentUser.asStateFlow()

    init {
        scope.launch {
            loadActiveAccount()
        }
    }

    private suspend fun loadActiveAccount() {
        withContext(Dispatchers.IO) {
            val activeId = authPrefs.getString(KEY_ACTIVE_USER_ID, null)
            if (activeId != null) {
                val savedProfile = userProfileDao.getUserProfile(activeId)
                if (savedProfile != null) {
                    _currentUser.value = UserAccount(
                        userId = savedProfile.userId,
                        email = savedProfile.email,
                        displayName = savedProfile.displayName,
                        isAnonymous = savedProfile.isAnonymous,
                        createdAt = savedProfile.createdAt,
                        lastSyncedAt = savedProfile.lastSyncedAt,
                        photoUrl = savedProfile.photoUrl
                    )
                    return@withContext
                }
            }

            // Check if any profile exists in database
            val latestProfile = userProfileDao.getActiveUser()
            if (latestProfile != null) {
                _currentUser.value = UserAccount(
                    userId = latestProfile.userId,
                    email = latestProfile.email,
                    displayName = latestProfile.displayName,
                    isAnonymous = latestProfile.isAnonymous,
                    createdAt = latestProfile.createdAt,
                    lastSyncedAt = latestProfile.lastSyncedAt,
                    photoUrl = latestProfile.photoUrl
                )
                authPrefs.edit().putString(KEY_ACTIVE_USER_ID, latestProfile.userId).apply()
            } else {
                // Seed local guest account
                val initialUser = DEFAULT_LOCAL_USER
                userProfileDao.insertProfile(
                    UserProfile(
                        userId = initialUser.userId,
                        email = initialUser.email,
                        displayName = initialUser.displayName,
                        isAnonymous = initialUser.isAnonymous,
                        createdAt = initialUser.createdAt,
                        updatedAt = initialUser.createdAt,
                        lastSyncedAt = 0L
                    )
                )
                authPrefs.edit().putString(KEY_ACTIVE_USER_ID, initialUser.userId).apply()
                _currentUser.value = initialUser
            }
        }
    }

    suspend fun createAccount(
        email: String,
        password: String,
        displayName: String
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        val trimmedName = displayName.trim().ifBlank { trimmedEmail.substringBefore("@") }

        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        // Check if email already registered
        val allProfiles = userProfileDao.getAllProfiles()
        if (allProfiles.any { it.email.equals(trimmedEmail, ignoreCase = true) && !it.isAnonymous }) {
            return@withContext Result.failure(IllegalStateException("Account with this email already exists."))
        }

        val userId = "usr_" + UUID.randomUUID().toString().replace("-", "").take(16)
        val salt = UUID.randomUUID().toString()
        val passwordHash = hashPassword(password, salt)

        // Store salt & hash securely in private app storage
        authPrefs.edit()
            .putString(PREFIX_USER_PASS + trimmedEmail, passwordHash)
            .putString(PREFIX_USER_SALT + trimmedEmail, salt)
            .putString(KEY_ACTIVE_USER_ID, userId)
            .apply()

        val newProfile = UserProfile(
            userId = userId,
            email = trimmedEmail,
            displayName = trimmedName,
            isAnonymous = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastSyncedAt = 0L
        )

        userProfileDao.insertProfile(newProfile)

        val account = UserAccount(
            userId = newProfile.userId,
            email = newProfile.email,
            displayName = newProfile.displayName,
            isAnonymous = false,
            createdAt = newProfile.createdAt,
            lastSyncedAt = 0L
        )

        _currentUser.value = account
        Result.success(account)
    }

    suspend fun signIn(email: String, password: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email cannot be empty."))
        }

        val storedSalt = authPrefs.getString(PREFIX_USER_SALT + trimmedEmail, null)
        val storedHash = authPrefs.getString(PREFIX_USER_PASS + trimmedEmail, null)

        if (storedSalt == null || storedHash == null) {
            return@withContext Result.failure(IllegalStateException("Account not found. Please create an account first."))
        }

        val computedHash = hashPassword(password, storedSalt)
        if (computedHash != storedHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password."))
        }

        val allProfiles = userProfileDao.getAllProfiles()
        val existingProfile = allProfiles.firstOrNull { it.email.equals(trimmedEmail, ignoreCase = true) }
        val userId = existingProfile?.userId ?: ("usr_" + UUID.randomUUID().toString().replace("-", "").take(16))

        val profile = existingProfile?.copy(updatedAt = System.currentTimeMillis()) ?: UserProfile(
            userId = userId,
            email = trimmedEmail,
            displayName = trimmedEmail.substringBefore("@"),
            isAnonymous = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastSyncedAt = 0L
        )

        userProfileDao.insertProfile(profile)
        authPrefs.edit().putString(KEY_ACTIVE_USER_ID, userId).apply()

        val account = UserAccount(
            userId = profile.userId,
            email = profile.email,
            displayName = profile.displayName,
            isAnonymous = false,
            createdAt = profile.createdAt,
            lastSyncedAt = profile.lastSyncedAt
        )

        _currentUser.value = account
        Result.success(account)
    }

    suspend fun recoverAccount(email: String, newPassword: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim().lowercase()
        if (trimmedEmail.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Email cannot be empty."))
        }
        if (newPassword.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }

        val allProfiles = userProfileDao.getAllProfiles()
        val existingProfile = allProfiles.firstOrNull { it.email.equals(trimmedEmail, ignoreCase = true) }
        val storedSalt = authPrefs.getString(PREFIX_USER_SALT + trimmedEmail, null)

        if (existingProfile == null && storedSalt == null) {
            return@withContext Result.failure(IllegalStateException("No account found matching this email address."))
        }

        val salt = storedSalt ?: UUID.randomUUID().toString()
        val newHash = hashPassword(newPassword, salt)

        authPrefs.edit()
            .putString(PREFIX_USER_SALT + trimmedEmail, salt)
            .putString(PREFIX_USER_PASS + trimmedEmail, newHash)
            .apply()

        val userId = existingProfile?.userId ?: ("usr_" + UUID.randomUUID().toString().replace("-", "").take(16))
        val profile = existingProfile?.copy(updatedAt = System.currentTimeMillis()) ?: UserProfile(
            userId = userId,
            email = trimmedEmail,
            displayName = trimmedEmail.substringBefore("@"),
            isAnonymous = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastSyncedAt = 0L
        )

        userProfileDao.insertProfile(profile)
        authPrefs.edit().putString(KEY_ACTIVE_USER_ID, userId).apply()

        val account = UserAccount(
            userId = profile.userId,
            email = profile.email,
            displayName = profile.displayName,
            isAnonymous = false,
            createdAt = profile.createdAt,
            lastSyncedAt = profile.lastSyncedAt
        )
        _currentUser.value = account
        Result.success(account)
    }

    suspend fun updateDisplayName(newName: String): Result<UserAccount> = withContext(Dispatchers.IO) {
        val current = _currentUser.value
        val updated = current.copy(displayName = newName.trim())
        userProfileDao.insertProfile(
            UserProfile(
                userId = updated.userId,
                email = updated.email,
                displayName = updated.displayName,
                isAnonymous = updated.isAnonymous,
                createdAt = updated.createdAt,
                updatedAt = System.currentTimeMillis(),
                lastSyncedAt = updated.lastSyncedAt
            )
        )
        _currentUser.value = updated
        Result.success(updated)
    }

    suspend fun updateLastSynced(timestamp: Long) = withContext(Dispatchers.IO) {
        val current = _currentUser.value
        val updated = current.copy(lastSyncedAt = timestamp)
        userProfileDao.updateLastSynced(current.userId, timestamp)
        _currentUser.value = updated
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        // Revert to local guest account without losing local data
        val guestAccount = DEFAULT_LOCAL_USER
        authPrefs.edit().remove(KEY_ACTIVE_USER_ID).apply()
        _currentUser.value = guestAccount
    }

    private fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$password:$salt"
        val bytes = md.digest(combined.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
