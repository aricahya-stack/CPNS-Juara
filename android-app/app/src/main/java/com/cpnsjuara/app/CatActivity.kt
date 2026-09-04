package com.cpnsjuara.app

import android.os.Bundle
import android.os.CountDownTimer
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CatActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var qs: List<QuizQuestion>
    private var idx = 0; private var score = 0; private var answered = 0
    private var timer: CountDownTimer? = null
    private lateinit var progress: TextView; private lateinit var question: WebView
    private lateinit var next: Button; private lateinit var time: TextView
    private lateinit var stimulusText: WebView; private lateinit var stimulusImage: ImageView
    private lateinit var stimulusSource: TextView; private lateinit var optionSelector: RichOptionSelector

    override fun onCreate(b: Bundle?) {
        super.onCreate(b); setContentView(R.layout.activity_cat)
        db = DatabaseHelper(this); qs = db.getMixedQuestions(30)
        progress = findViewById(R.id.txtCatProgress); question = findViewById(R.id.webCatQuestion)
        next = findViewById(R.id.btnCatNext); time = findViewById(R.id.txtCatTimer)
        stimulusText = findViewById(R.id.webCatStimulusText); stimulusImage = findViewById(R.id.imgCatStimulus)
        stimulusSource = findViewById(R.id.txtCatStimulusSource)
        optionSelector = RichOptionSelector(this, findViewById<LinearLayout>(R.id.catOptionContainer))
        RichContentRenderer.prepare(question); RichContentRenderer.prepare(stimulusText)
        if (qs.isEmpty()) { RichContentRenderer.render(question, "<p>Belum ada soal offline. Download paket terlebih dahulu.</p>"); next.isEnabled=false; return }
        timer = object: CountDownTimer(30*60*1000L,1000L){
            override fun onTick(ms:Long){time.text="%02d:%02d".format(ms/60000,(ms/1000)%60)}
            override fun onFinish(){finishExam()}
        }.start()
        show(); next.setOnClickListener { save(); idx++; if(idx>=qs.size) finishExam() else show() }
    }

    private fun show(){
        val q=qs[idx]
        progress.text=buildString{append(q.category);if(q.blueprintCode.isNotBlank())append(" • ${q.blueprintCode}");append(" • ${idx+1}/${qs.size}")}
        StimulusRenderer.render(this,q,stimulusText,stimulusImage,stimulusSource)
        RichContentRenderer.render(question,q.prompt)
        optionSelector.render(q.options)
    }
    private fun save(){
        val code=optionSelector.selectedCode() ?: return
        val q=qs[idx]; val o=q.options.first{it.code==code}; db.saveResult(q,o);score+=o.score;answered++
    }
    private fun finishExam(){
        timer?.cancel(); StimulusRenderer.clear(stimulusText,stimulusImage,stimulusSource)
        time.text="SELESAI";progress.text="Simulasi CAT selesai"
        RichContentRenderer.render(question,"<p>Dijawab: $answered/${qs.size}</p><p><strong>Total skor: $score</strong></p>")
        optionSelector.clear();next.text="Tutup";next.setOnClickListener{ProgressSync.run(this);finish()}
    }
    override fun onDestroy(){timer?.cancel();super.onDestroy()}
}
