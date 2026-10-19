package com.dailyrap.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.dailyrap.app.data.local.SettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wibracja przy dotykaniu klawiszy (RapWord) i kafelków (RapConnect).
 * Respektuje przełącznik „Wibracje" z ustawień apki.
 */
@Singleton
class Haptics @Inject constructor(
    @ApplicationContext private val context: Context,
    settingsDataStore: SettingsDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val enabled = settingsDataStore.vibrationEnabled
        .stateIn(scope, SharingStarted.Eagerly, true)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /** Krótkie „kliknięcie" przy dotknięciu klawisza lub kafelka. */
    fun tap() {
        if (!enabled.value) return
        val v = vibrator?.takeIf { it.hasVibrator() } ?: return
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        } else {
            VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        v.vibrate(effect)
    }
}
