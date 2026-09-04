package com.cpnsjuara.app

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.RadioButton

class RichOptionSelector(
    private val activity: Activity,
    private val container: LinearLayout
) {
    private val radios = mutableListOf<RadioButton>()
    private var selected: String? = null

    fun render(options: List<QuizOption>) {
        clear()
        options.forEach { option ->
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
                setPadding(dp(10), dp(8), dp(10), dp(8))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    setStroke(dp(1), Color.rgb(220, 228, 239))
                    cornerRadius = dp(12).toFloat()
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, dp(10)) }
            }
            val radio = RadioButton(activity).apply {
                text = option.code
                tag = option.code
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(dp(54), ViewGroup.LayoutParams.WRAP_CONTENT)
                setOnClickListener { select(option.code) }
            }
            radios.add(radio)
            val web = WebView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(56), 1f)
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) select(option.code)
                    false
                }
            }
            RichContentRenderer.prepare(web)
            RichContentRenderer.render(web, option.contentHtml)
            row.setOnClickListener { select(option.code) }
            row.addView(radio)
            row.addView(web)
            container.addView(row)
        }
    }

    fun selectedCode(): String? = selected

    fun clear() {
        selected = null
        radios.clear()
        container.removeAllViews()
    }

    private fun select(code: String) {
        selected = code
        radios.forEach { it.isChecked = it.tag == code }
    }

    private fun dp(value: Int): Int =
        (value * activity.resources.displayMetrics.density).toInt()
}
