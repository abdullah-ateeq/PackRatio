package com.example.packratio.util

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticUtil {

    fun performLight(hapticFeedback: HapticFeedback?, enabled: Boolean) {
        if (!enabled || hapticFeedback == null) return
        try {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {
            // Ignore any haptic failures
        }
    }

    fun performMedium(hapticFeedback: HapticFeedback?, enabled: Boolean) {
        if (!enabled || hapticFeedback == null) return
        try {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
            // Ignore any haptic failures
        }
    }
}
