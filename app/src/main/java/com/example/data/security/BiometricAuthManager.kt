package com.example.data.security

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricStatus {
    READY,
    NOT_ENROLLED,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    UNSUPPORTED
}

object BiometricAuthManager {

    private const val AUTH_TYPES =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun checkBiometricStatus(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(AUTH_TYPES)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.READY
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.UNSUPPORTED
            else -> BiometricStatus.UNSUPPORTED
        }
    }

    fun getStatusDescription(status: BiometricStatus): String {
        return when (status) {
            BiometricStatus.READY -> "Biometric sensor ready & enrolled"
            BiometricStatus.NOT_ENROLLED -> "No fingerprint or face registered in Android Settings"
            BiometricStatus.NO_HARDWARE -> "No biometric hardware detected on device"
            BiometricStatus.HW_UNAVAILABLE -> "Biometric sensor is currently unavailable"
            BiometricStatus.UNSUPPORTED -> "Biometrics not supported on this device"
        }
    }

    fun openSecuritySettings(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, AUTH_TYPES)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(enrollIntent)
            } else {
                val secIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(secIntent)
            }
        } catch (_: Exception) {
            try {
                val secIntent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(secIntent)
            } catch (_: Exception) {}
        }
    }

    fun promptBiometricAuthentication(
        activity: FragmentActivity,
        title: String = "FinFlow Protected",
        subtitle: String = "Verify your biometric identity to unlock",
        negativeButtonText: String = "Use PIN / Password",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val status = checkBiometricStatus(activity)
        if (status != BiometricStatus.READY) {
            val description = getStatusDescription(status)
            onError(description)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    // Clean user dismissal / fallback to manual input
                    onError("")
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometric not recognized. Please try again.")
            }
        }

        try {
            val prompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(AUTH_TYPES)
                .build()

            prompt.authenticate(promptInfo)
        } catch (e: Exception) {
            onError(e.message ?: "Authentication failed to start")
        }
    }
}
