package com.cpnsjuara.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class RankingActivity:AppCompatActivity(){private val ex=Executors.newSingleThreadExecutor();override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_ranking);val out=findViewById<TextView>(R.id.txtRanking);AuthToken.get{token->if(token==null){out.text="Ranking memerlukan Login Google.";return@get};ex.execute{try{val a=ApiClient.ranking(Prefs.getBaseUrl(this),token).getJSONArray("ranking");val sb=StringBuilder();for(i in 0 until a.length()){val x=a.getJSONObject(i);sb.append("${i+1}. ${x.optString("display_name","Pejuang ASN")} — ${x.optInt("score",0)} poin\n")};runOnUiThread{out.text=if(sb.isEmpty())"Belum ada data ranking." else sb.toString()}}catch(e:Exception){runOnUiThread{out.text="Ranking belum tersedia: ${e.message}"}}}}};override fun onDestroy(){ex.shutdown();super.onDestroy()}}
