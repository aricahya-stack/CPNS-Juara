package com.cpnsjuara.app

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executors

object AuthSync {
    private val executor = Executors.newSingleThreadExecutor()

    fun syncCurrentUser(
        context: Context,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        val user = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }
        if (user == null) {
            onResult?.invoke(false, "Belum login dengan Google.")
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { tokenResult ->
                val token = tokenResult.token
                if (token.isNullOrBlank()) {
                    onResult?.invoke(false, "Firebase ID token kosong.")
                    return@addOnSuccessListener
                }

                val baseUrl = Prefs.getBaseUrl(context)
                executor.execute {
                    try {
                        val response = ApiClient.syncFirebaseUser(baseUrl, token)
                        val message =
                            response.optString("message", "Akun tersinkron.")
                        Log.d("CPNSJUARA_AUTH", message)
                        onResult?.invoke(true, message)
                    } catch (e: Exception) {
                        Log.w("CPNSJUARA_AUTH", "Backend sync gagal", e)
                        onResult?.invoke(false, e.message ?: "Sync gagal")
                    }
                }
            }
            .addOnFailureListener { error ->
                onResult?.invoke(
                    false,
                    error.message ?: "Gagal mengambil ID token."
                )
            }
    }
}
