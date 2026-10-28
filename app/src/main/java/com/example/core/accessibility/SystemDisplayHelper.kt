package com.example.core.accessibility

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object SystemDisplayHelper {

    // Android Secure Settings keys for Grayscale / Monochromacy
    private const val SETTING_DALTONIZER_ENABLED = "accessibility_display_daltonizer_enabled"
    private const val SETTING_DALTONIZER = "accessibility_display_daltonizer"
    private const val MONOCHROMACY_DALTONIZER_VALUE = 0 // 0 = Monochromacy / Grayscale

    /**
     * Checks if the app has been granted WRITE_SECURE_SETTINGS (via ADB or System/Root)
     */
    fun hasWriteSecureSettingsPermission(context: Context): Boolean {
        return try {
            val permissionCheck = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
            permissionCheck == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Checks if system-wide Monochrome / Grayscale is currently active on the device
     */
    fun isDeviceMonochromeActive(context: Context): Boolean {
        return try {
            val enabled = Settings.Secure.getInt(
                context.contentResolver,
                SETTING_DALTONIZER_ENABLED,
                0
            ) == 1
            val mode = Settings.Secure.getInt(
                context.contentResolver,
                SETTING_DALTONIZER,
                -1
            )
            enabled && mode == MONOCHROMACY_DALTONIZER_VALUE
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Attempts to programmatically toggle system-wide Monochrome (Grayscale) across the entire Android OS.
     * Returns true if successfully applied, false if WRITE_SECURE_SETTINGS permission is required.
     */
    fun setDeviceMonochrome(context: Context, enabled: Boolean): Boolean {
        return try {
            if (enabled) {
                Settings.Secure.putInt(
                    context.contentResolver,
                    SETTING_DALTONIZER,
                    MONOCHROMACY_DALTONIZER_VALUE
                )
                Settings.Secure.putInt(
                    context.contentResolver,
                    SETTING_DALTONIZER_ENABLED,
                    1
                )
            } else {
                Settings.Secure.putInt(
                    context.contentResolver,
                    SETTING_DALTONIZER_ENABLED,
                    0
                )
            }
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns the exact ADB terminal command for user to copy/paste to grant direct system-wide grayscale control.
     */
    fun getAdbCommand(context: Context): String {
        return "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
    }

    /**
     * Launches Android's official Color Correction / Accessibility settings screen so user can toggle Grayscale with 1 tap.
     */
    fun openColorCorrectionSettings(context: Context) {
        val intentsToTry = listOf(
            Intent("android.settings.COLOR_CORRECTION_SETTINGS"),
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            Intent(Settings.ACTION_DISPLAY_SETTINGS)
        )

        for (intent in intentsToTry) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (_: Exception) {}
        }

        Toast.makeText(
            context,
            "Open Settings > Accessibility > Color correction > Grayscale",
            Toast.LENGTH_LONG
        ).show()
    }

    fun openAccessibilitySettings(context: Context) {
        openColorCorrectionSettings(context)
    }
}
