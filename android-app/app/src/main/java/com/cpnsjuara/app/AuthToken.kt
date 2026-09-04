package com.cpnsjuara.app

import com.google.firebase.auth.FirebaseAuth

object AuthToken {
    fun get(onResult: (String?) -> Unit) {
        val user = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Exception) {
            null
        }
        if (user == null) {
            onResult(null)
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { onResult(it.token) }
            .addOnFailureListener { onResult(null) }
    }
}
