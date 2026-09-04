package com.cpnsjuara.app

import android.content.Context

object PremiumPrefs {
    private const val FILE = "cpnsjuara_premium"
    private const val PREMIUM = "premium"
    private const val PRODUCT = "product"
    private const val EXPIRY = "expiry"

    fun isPremium(context: Context): Boolean {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val premium = prefs.getBoolean(PREMIUM, false)
        val expiry = prefs.getLong(EXPIRY, 0L)
        return premium && (expiry == 0L || expiry > System.currentTimeMillis())
    }

    fun save(context: Context, premium: Boolean, productId: String?, expiryMillis: Long?) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putBoolean(PREMIUM, premium)
            .putString(PRODUCT, productId ?: "")
            .putLong(EXPIRY, expiryMillis ?: 0L)
            .apply()
    }
}
