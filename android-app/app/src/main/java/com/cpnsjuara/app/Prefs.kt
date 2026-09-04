package com.cpnsjuara.app

import android.content.Context

object Prefs {
    private const val FILE = "cpnsjuara_prefs"
    private const val BASE_URL = "base_url"
    private const val GUEST_MODE = "guest_mode"

    fun getBaseUrl(context: Context): String {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(BASE_URL, "http://10.0.2.2:8000")
            ?.trimEnd('/')
            ?: "http://10.0.2.2:8000"
    }

    fun setBaseUrl(context: Context, value: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(BASE_URL, value.trim().trimEnd('/'))
            .apply()
    }

    fun isGuest(context: Context): Boolean {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getBoolean(GUEST_MODE, false)
    }

    fun setGuest(context: Context, enabled: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(GUEST_MODE, enabled)
            .apply()
    }
}
