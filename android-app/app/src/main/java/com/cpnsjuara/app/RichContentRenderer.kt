package com.cpnsjuara.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

object RichContentRenderer {
    private fun document(html: String): String = """
        <!doctype html>
        <html><head><meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        <style>
        html,body{margin:0;padding:0;background:transparent;color:#17243B;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;font-size:16px;line-height:1.6}
        p{margin:0 0 10px} img{max-width:100%;height:auto;border-radius:8px;display:block;margin:8px auto}
        table{border-collapse:collapse;width:100%;margin:8px 0} td,th{border:1px solid #D9E2EE;padding:6px;vertical-align:top}
        pre{white-space:pre-wrap;background:#F5F7FA;padding:9px;border-radius:7px} code{font-family:monospace}
        blockquote{margin:8px 0;padding:8px 12px;border-left:3px solid #1769D2;background:#F6FAFF}
        .cpns-math-block{display:block;text-align:center;overflow-x:auto;margin:10px 0}.cpns-math-inline{display:inline-block}
        math{font-size:1.08em} a{color:#1769D2;text-decoration:none}
        </style></head><body>${html.ifBlank { "&nbsp;" }}</body></html>
    """.trimIndent()

    @SuppressLint("SetJavaScriptEnabled")
    fun prepare(webView: WebView) {
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.settings.javaScriptEnabled = true // only used to measure sanitized local HTML height
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = false
        webView.settings.domStorageEnabled = false
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = true
            override fun onPageFinished(view: WebView, url: String?) {
                view.evaluateJavascript("Math.max(document.body.scrollHeight,document.documentElement.scrollHeight).toString()") { raw ->
                    val cssPx = raw?.replace("\\\"", "")?.toFloatOrNull() ?: return@evaluateJavascript
                    val px = (cssPx * view.resources.displayMetrics.density).toInt().coerceAtLeast(dp(view.context, 44))
                    view.layoutParams = view.layoutParams.apply { height = px }
                    view.requestLayout()
                }
            }
        }
    }

    fun render(webView: WebView, html: String) {
        if (html.isBlank()) {
            clear(webView)
            return
        }
        webView.visibility = View.VISIBLE
        webView.loadDataWithBaseURL("file:///", document(html), "text/html", "UTF-8", null)
    }

    fun clear(webView: WebView) {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.visibility = View.GONE
    }

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
