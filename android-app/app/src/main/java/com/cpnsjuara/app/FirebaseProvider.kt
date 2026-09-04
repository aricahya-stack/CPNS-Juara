package com.cpnsjuara.app

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

object FirebaseProvider {
    fun authOrNull(context: Context): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            if (FirebaseApp.getApps(context).isEmpty()) null
            else FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    fun isConfigured(context: Context): Boolean = authOrNull(context) != null
}
