package com.cpnsjuara.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity

class QuickTestActivity:AppCompatActivity(){
    private lateinit var categories:RadioGroup
    override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_quick_test);categories=findViewById(R.id.radioQuickCategory);findViewById<Button>(R.id.btnQuick5).setOnClickListener{go(5)};findViewById<Button>(R.id.btnQuick10).setOnClickListener{go(10)};findViewById<Button>(R.id.btnQuick20).setOnClickListener{go(20)}}
    private fun go(limit:Int){val checked=findViewById<RadioButton>(categories.checkedRadioButtonId);val category=checked?.tag?.toString()?:"ALL";startActivity(Intent(this,QuizActivity::class.java).putExtra("category",category).putExtra("limit",limit))}
}
