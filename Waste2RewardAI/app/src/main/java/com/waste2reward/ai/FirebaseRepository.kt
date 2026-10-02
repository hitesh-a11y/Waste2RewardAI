package com.waste2reward.ai

import android.content.Context

/**
 * Optional Firebase repository. Stubbed for minimal build without Firebase dependency.
 * Falls back to no-op when Firebase is not present.
 */
class FirebaseRepository(context: Context) {
    fun saveScan(userId: String, result: ScanResult, onDone: (Boolean) -> Unit = {}) {
        // No-op when Firebase is not on the classpath
        onDone(false)
    }
}
