package com.cpnsjuara.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_profile)
        auth = FirebaseProvider.authOrNull(this)
        credentialManager = CredentialManager.create(this)
        render()

        findViewById<Button>(R.id.btnSyncProfile).setOnClickListener {
            if (auth?.currentUser == null) {
                Toast.makeText(
                    this,
                    if (auth == null) {
                        "Firebase belum dikonfigurasi. Guest Mode tetap dapat digunakan."
                    } else {
                        "Mode tamu tidak memiliki cloud sync."
                    },
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            findViewById<TextView>(R.id.txtProfileStatus).text =
                "Menyinkronkan profil & progres..."
            AuthSync.syncCurrentUser(this) { _, _ ->
                ProgressSync.run(this) { ok, msg ->
                    runOnUiThread {
                        findViewById<TextView>(R.id.txtProfileStatus).text =
                            if (ok) "✓ $msg" else "Sync tertunda: $msg"
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnPremiumFromProfile).setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        findViewById<Button>(R.id.btnLoginOrLogout).setOnClickListener {
            if (auth?.currentUser != null) {
                logout()
            } else {
                Prefs.setGuest(this, false)
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        auth = FirebaseProvider.authOrNull(this)
        render()
    }

    private fun render() {
        val user = auth?.currentUser
        val name = findViewById<TextView>(R.id.txtProfileName)
        val email = findViewById<TextView>(R.id.txtProfileEmail)
        val status = findViewById<TextView>(R.id.txtProfileStatus)
        val action = findViewById<Button>(R.id.btnLoginOrLogout)

        if (user != null) {
            name.text = user.displayName ?: "Pengguna CPNS JUARA"
            email.text = user.email ?: "Akun Google"
            status.text = if (PremiumPrefs.isPremium(this)) {
                "🏆 PREMIUM • cloud sync aktif"
            } else {
                "FREE • cloud sync tersedia"
            }
            action.text = "Keluar dari Akun Google"
        } else {
            name.text = "Mode Tamu"
            email.text = "Data latihan tersimpan lokal di perangkat"
            status.text = if (auth == null) {
                "Firebase belum dikonfigurasi. Guest Mode siap digunakan."
            } else {
                "Login Google untuk backup, ranking, dan Premium."
            }
            action.text = "Masuk dengan Google"
        }
    }

    private fun logout() {
        auth?.signOut()
        Prefs.setGuest(this, false)
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(
                    ClearCredentialStateRequest()
                )
            } catch (_: Exception) {
            }
            startActivity(
                Intent(this@ProfileActivity, LoginActivity::class.java)
            )
            finishAffinity()
        }
    }
}
