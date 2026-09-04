package com.cpnsjuara.app

import android.content.Context
import java.util.concurrent.Executors

object ProgressSync {
    private val executor = Executors.newSingleThreadExecutor()
    fun run(context: Context, onResult: ((Boolean, String) -> Unit)? = null) {
        AuthToken.get { token ->
            if (token == null) { onResult?.invoke(false, "Mode tamu: progres hanya lokal."); return@get }
            val db = DatabaseHelper(context)
            val pending = db.pendingResultsJson()
            if (pending.length() == 0) { onResult?.invoke(true, "Tidak ada progres baru."); return@get }
            val ids = (0 until pending.length()).map { pending.getJSONObject(it).getInt("local_id") }
            executor.execute {
                try {
                    val response = ApiClient.syncProgress(Prefs.getBaseUrl(context), token, pending)
                    if (response.optBoolean("ok", false)) {
                        db.markResultsSynced(ids); onResult?.invoke(true, "${ids.size} hasil tersinkron.")
                    } else onResult?.invoke(false, "Server menolak sync.")
                } catch (e: Exception) { onResult?.invoke(false, e.message ?: "Sync gagal") }
            }
        }
    }
}
