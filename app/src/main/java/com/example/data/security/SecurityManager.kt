package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

enum class SecurityLockType {
    NONE,
    PIN,
    PASSWORD
}

class SecurityManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finflow_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOCK_TYPE = "security_lock_type"
        private const val KEY_CREDENTIAL_HASH = "security_credential_hash"
        private const val KEY_BIOMETRIC_ENABLED = "security_biometric_enabled"
        private const val KEY_AUTOLOCK_RESUME = "security_autolock_resume"
        private const val SALT = "FinFlow_Security_Salt_V1_2026"
    }

    fun getLockType(): SecurityLockType {
        val typeStr = prefs.getString(KEY_LOCK_TYPE, SecurityLockType.NONE.name)
        return try {
            SecurityLockType.valueOf(typeStr ?: SecurityLockType.NONE.name)
        } catch (e: Exception) {
            SecurityLockType.NONE
        }
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun isAutoLockOnResume(): Boolean = prefs.getBoolean(KEY_AUTOLOCK_RESUME, true)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun setAutoLockOnResume(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTOLOCK_RESUME, enabled).apply()
    }

    fun hasCredentialSet(): Boolean {
        return getLockType() != SecurityLockType.NONE &&
                !prefs.getString(KEY_CREDENTIAL_HASH, null).isNullOrBlank()
    }

    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false
        val hash = hash(pin)
        prefs.edit()
            .putString(KEY_LOCK_TYPE, SecurityLockType.PIN.name)
            .putString(KEY_CREDENTIAL_HASH, hash)
            .apply()
        return true
    }

    fun setPassword(password: String): Boolean {
        if (password.length < 4) return false
        val hash = hash(password)
        prefs.edit()
            .putString(KEY_LOCK_TYPE, SecurityLockType.PASSWORD.name)
            .putString(KEY_CREDENTIAL_HASH, hash)
            .apply()
        return true
    }

    fun verifyCredential(input: String): Boolean {
        val storedHash = prefs.getString(KEY_CREDENTIAL_HASH, null) ?: return false
        return hash(input) == storedHash
    }

    fun removeLock() {
        prefs.edit()
            .putString(KEY_LOCK_TYPE, SecurityLockType.NONE.name)
            .remove(KEY_CREDENTIAL_HASH)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    private fun hash(input: String): String {
        val bytes = (input + SALT).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
