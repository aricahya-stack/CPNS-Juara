package com.cpnsjuara.app

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var count: TextView
    private lateinit var account: TextView
    private lateinit var badge: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val user = FirebaseProvider.authOrNull(this)?.currentUser
        if (user == null && !Prefs.isGuest(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        db = DatabaseHelper(this)
        count = findViewById(R.id.txtLocalCount)
        account = findViewById(R.id.txtAccount)
        badge = findViewById(R.id.txtPremiumBadge)

        findViewById<Button>(R.id.btnPackages).setOnClickListener {
            startActivity(Intent(this, PackageActivity::class.java))
        }
        findViewById<Button>(R.id.btnTWK).setOnClickListener { quiz("TWK") }
        findViewById<Button>(R.id.btnTIU).setOnClickListener { quiz("TIU") }
        findViewById<Button>(R.id.btnTKP).setOnClickListener { quiz("TKP") }
        findViewById<Button>(R.id.btnQuickTest).setOnClickListener {
            startActivity(Intent(this, QuickTestActivity::class.java))
        }
        findViewById<Button>(R.id.btnWrong).setOnClickListener {
            startActivity(
                Intent(this, QuizActivity::class.java)
                    .putExtra("category", "WRONG")
                    .putExtra("limit", 20)
            )
        }
        findViewById<Button>(R.id.btnSaved).setOnClickListener {
            startActivity(
                Intent(this, QuizActivity::class.java)
                    .putExtra("category", "BOOKMARK")
                    .putExtra("limit", 50)
            )
        }
        findViewById<Button>(R.id.btnCat).setOnClickListener {
            startActivity(Intent(this, CatActivity::class.java))
        }
        findViewById<Button>(R.id.btnRanking).setOnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
        }
        findViewById<Button>(R.id.btnPremium).setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }
        findViewById<Button>(R.id.btnStats).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        count.text = "Soal offline: ${db.countQuestions()}"
        val user = FirebaseProvider.authOrNull(this)?.currentUser
        account.text = if (user != null) {
            "Halo, ${user.displayName ?: "Pejuang ASN"} • Akun Google"
        } else {
            "Mode Tamu • progres lokal"
        }
        badge.text = if (PremiumPrefs.isPremium(this)) "🏆 PREMIUM" else "FREE"

        val banner = findViewById<ViewGroup>(R.id.bannerContainer)
        if (user != null) {
            ProgressSync.run(this)
            EntitlementSync.run(this) { _, _ ->
                runOnUiThread {
                    badge.text =
                        if (PremiumPrefs.isPremium(this)) "🏆 PREMIUM" else "FREE"
                    AdsManager.initialize(this)
                    AdsManager.attachBanner(this, banner)
                }
            }
        } else {
            AdsManager.initialize(this)
            AdsManager.attachBanner(this, banner)
        }
    }

    private fun quiz(category: String) {
        startActivity(
            Intent(this, QuizActivity::class.java)
                .putExtra("category", category)
        )
    }
}
