package com.cpnsjuara.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private lateinit var credentialManager: CredentialManager
    private lateinit var txtStatus: TextView
    private lateinit var btnGoogle: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseProvider.authOrNull(this)

        if (auth?.currentUser != null || Prefs.isGuest(this)) {
            openMain()
            return
        }

        setContentView(R.layout.activity_login)
        credentialManager = CredentialManager.create(this)
        txtStatus = findViewById(R.id.txtLoginStatus)
        btnGoogle = findViewById(R.id.btnGoogleLogin)

        val firebaseReady = auth != null && resolveWebClientId().isNotBlank()
        btnGoogle.isEnabled = firebaseReady
        txtStatus.text = if (firebaseReady) {
            "Masuk dengan Google atau lanjut sebagai tamu."
        } else {
            "Firebase belum dikonfigurasi. Guest Mode sudah bisa dipakai; Google Login aktif setelah google-services.json dipasang."
        }

        btnGoogle.setOnClickListener { signInWithGoogle() }

        findViewById<Button>(R.id.btnGuest).setOnClickListener {
            Prefs.setGuest(this, true)
            auth?.signOut()
            openMain()
        }
    }

    private fun resolveWebClientId(): String {
        val id = resources.getIdentifier(
            "default_web_client_id",
            "string",
            packageName
        )
        return if (id != 0) getString(id) else ""
    }

    private fun signInWithGoogle() {
        val firebaseAuth = auth
        val clientId = resolveWebClientId()
        if (firebaseAuth == null || clientId.isBlank()) {
            txtStatus.text =
                "Google Login belum siap. Tambahkan google-services.json dari Firebase terlebih dahulu."
            return
        }

        btnGoogle.isEnabled = false
        txtStatus.text = "Membuka akun Google..."

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(clientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@LoginActivity,
                    request = request
                )
                val credential = result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(
                        googleCredential.idToken,
                        null
                    )

                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this@LoginActivity) { task ->
                            btnGoogle.isEnabled = true
                            if (task.isSuccessful) {
                                Prefs.setGuest(this@LoginActivity, false)
                                txtStatus.text = "Login berhasil."
                                AuthSync.syncCurrentUser(this@LoginActivity)
                                openMain()
                            } else {
                                txtStatus.text =
                                    task.exception?.message ?: "Login Google gagal."
                            }
                        }
                } else {
                    btnGoogle.isEnabled = true
                    txtStatus.text = "Credential Google tidak dikenali."
                }
            } catch (e: GetCredentialException) {
                btnGoogle.isEnabled = true
                txtStatus.text =
                    "Login dibatalkan / gagal: ${e.message ?: e.type}"
            } catch (e: Exception) {
                btnGoogle.isEnabled = true
                txtStatus.text = "Login gagal: ${e.message}"
            }
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
