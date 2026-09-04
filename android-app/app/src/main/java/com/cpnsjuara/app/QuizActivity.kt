package com.cpnsjuara.app

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var qs: List<QuizQuestion>
    private var idx = 0
    private var score = 0
    private var count = 0

    private lateinit var progress: TextView
    private lateinit var question: WebView
    private lateinit var feedback: TextView
    private lateinit var explanation: WebView
    private lateinit var submit: Button
    private lateinit var bookmark: Button
    private lateinit var hint: Button
    private lateinit var stimulusText: WebView
    private lateinit var stimulusImage: ImageView
    private lateinit var stimulusSource: TextView
    private lateinit var optionSelector: RichOptionSelector

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_quiz)
        db = DatabaseHelper(this)
        val category = intent.getStringExtra("category") ?: "TIU"
        val limit = intent.getIntExtra("limit", 10)
        qs = when (category) {
            "ALL" -> db.getMixedQuestions(limit)
            "BOOKMARK" -> db.getBookmarkedQuestions(limit)
            "WRONG" -> db.getWrongQuestions(limit)
            else -> db.getQuestions(category, limit)
        }

        progress = findViewById(R.id.txtProgress)
        question = findViewById(R.id.webQuestion)
        feedback = findViewById(R.id.txtFeedback)
        explanation = findViewById(R.id.webExplanation)
        submit = findViewById(R.id.btnSubmit)
        bookmark = findViewById(R.id.btnBookmark)
        hint = findViewById(R.id.btnHint)
        stimulusText = findViewById(R.id.webStimulusText)
        stimulusImage = findViewById(R.id.imgStimulus)
        stimulusSource = findViewById(R.id.txtStimulusSource)
        optionSelector = RichOptionSelector(this, findViewById<LinearLayout>(R.id.optionContainer))

        RichContentRenderer.prepare(question)
        RichContentRenderer.prepare(explanation)
        RichContentRenderer.prepare(stimulusText)

        if (qs.isEmpty()) {
            RichContentRenderer.render(question, "<p>Belum ada soal di HP. Download paket terlebih dahulu.</p>")
            submit.isEnabled = false; bookmark.isEnabled = false; hint.isEnabled = false
            return
        }
        show()
        submit.setOnClickListener { answer() }
        bookmark.setOnClickListener { updateBookmark(db.toggleBookmark(qs[idx].id)) }
        hint.setOnClickListener {
            AdsManager.showRewardedHint(this,
                {
                    feedback.text = "Petunjuk / Pembahasan"
                    RichContentRenderer.render(explanation, qs[idx].explanation.ifBlank { "<p>Belum ada pembahasan.</p>" })
                },
                { feedback.text = "Rewarded ad belum tersedia." }
            )
        }
    }

    private fun show() {
        val q = qs[idx]
        progress.text = buildString {
            append(q.category)
            if (q.blueprintCode.isNotBlank()) append(" • ${q.blueprintCode}")
            append(" • Soal ${idx + 1}/${qs.size}")
        }
        StimulusRenderer.render(this, q, stimulusText, stimulusImage, stimulusSource)
        RichContentRenderer.render(question, q.prompt)
        feedback.text = ""
        RichContentRenderer.clear(explanation)
        updateBookmark(db.isBookmarked(q.id))
        optionSelector.render(q.options)
    }

    private fun answer() {
        val code = optionSelector.selectedCode()
        if (code == null) { feedback.text = "Pilih jawaban terlebih dahulu."; return }
        val q = qs[idx]
        val o = q.options.first { it.code == code }
        db.saveResult(q, o); score += o.score; count++
        feedback.text = if (q.scoringMode == "weighted") {
            "Skor pilihan: ${o.score}"
        } else if (o.isCorrect) {
            "Benar ✓"
        } else {
            "Belum tepat. Jawaban: ${q.options.firstOrNull { it.isCorrect }?.code ?: "-"}"
        }
        RichContentRenderer.render(explanation, q.explanation)
        val go = { idx++; if (idx >= qs.size) finishSession() else show() }
        if (count == 5 && !PremiumPrefs.isPremium(this)) AdsManager.maybeShowInterstitial(this, count, go)
        else submit.postDelayed({ go() }, 450)
    }

    private fun finishSession() {
        StimulusRenderer.clear(stimulusText, stimulusImage, stimulusSource)
        RichContentRenderer.render(question, "<p><strong>Sesi selesai.</strong></p><p>Skor sesi: $score</p>")
        optionSelector.clear(); bookmark.isEnabled = false; hint.isEnabled = false
        submit.text = "Tutup"; ProgressSync.run(this); submit.setOnClickListener { finish() }
    }

    private fun updateBookmark(x: Boolean) { bookmark.text = if (x) "★ Tersimpan" else "☆ Simpan Soal" }
}
