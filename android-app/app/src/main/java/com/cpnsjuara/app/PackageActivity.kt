package com.cpnsjuara.app

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors

class PackageActivity : AppCompatActivity() {
    private val executor=Executors.newSingleThreadExecutor()
    private lateinit var container:LinearLayout
    private lateinit var status:TextView
    private lateinit var db:DatabaseHelper

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);setContentView(R.layout.activity_packages)
        db=DatabaseHelper(this);container=findViewById(R.id.packageContainer);status=findViewById(R.id.txtStatus)
        findViewById<Button>(R.id.btnRefresh).setOnClickListener{loadPackages()}
    }

    private fun loadPackages(){
        status.text="Menghubungi server...";container.removeAllViews();val baseUrl=Prefs.getBaseUrl(this)
        executor.execute{
            try{val packages=ApiClient.fetchPackages(baseUrl);runOnUiThread{status.text="${packages.size} paket ditemukan.";packages.forEach{addPackageView(it)}}}
            catch(e:Exception){runOnUiThread{status.text="Gagal: ${e.message}"}}
        }
    }

    private fun addPackageView(info:PackageInfo){
        val wrapper=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,20,20,20);setBackgroundColor(Color.WHITE);layoutParams=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,0,0,18)}}
        val title=TextView(this).apply{text=(if(info.isPremium)"🏆 " else "")+info.name;textSize=18f;setTextColor(Color.rgb(23,59,108))}
        val detail=TextView(this).apply{text="Version ${info.version} • ${info.questionCount} soal\n${if(info.isPremium)"Premium • " else ""}${info.description}"}
        val download=Button(this).apply{text=if(info.isPremium)"Download Premium" else "Download / Update";setOnClickListener{downloadPackage(info)}}
        wrapper.addView(title);wrapper.addView(detail);wrapper.addView(download);container.addView(wrapper)
    }

    private fun downloadPackage(info:PackageInfo){
        status.text="Download ${info.name}...";val baseUrl=Prefs.getBaseUrl(this)
        if(!info.isPremium){executor.execute{downloadWithToken(info,baseUrl,null)};return}
        AuthToken.get{token->
            if(token==null){runOnUiThread{status.text="Paket Premium memerlukan Login Google dan subscription aktif."};return@get}
            executor.execute{downloadWithToken(info,baseUrl,token)}
        }
    }

    private fun downloadWithToken(info:PackageInfo,baseUrl:String,token:String?){
        try{val(version,questions)=ApiClient.fetchPackage(baseUrl,info.id,token);db.replacePackage(info.id,version,questions);runOnUiThread{status.text="✓ ${info.name}: ${questions.size} soal tersimpan offline."}}
        catch(e:Exception){runOnUiThread{status.text="Download gagal: ${e.message}"}}
    }

    override fun onDestroy(){executor.shutdown();super.onDestroy()}
}
