package com.cpnsjuara.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatsActivity:AppCompatActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_stats);val db=DatabaseHelper(this);val(answered,_,score)=db.stats();val(singleAnswered,singleCorrect)=db.singleAnswerStats();val accuracy=if(singleAnswered==0)0 else singleCorrect*100/singleAnswered;findViewById<TextView>(R.id.txtStats).text="""Soal dikerjakan
$answered

TWK/TIU single-answer
$singleAnswered soal • $singleCorrect benar

Akurasi TWK/TIU
$accuracy%

Total skor tersimpan
$score

Catatan: TKP memakai weighted scoring sehingga tidak dihitung sebagai benar/salah pada akurasi.""".trimIndent()}}
