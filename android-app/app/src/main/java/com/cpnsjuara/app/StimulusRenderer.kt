package com.cpnsjuara.app

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object StimulusRenderer {
    private val executor = Executors.newCachedThreadPool()

    fun render(
        activity: Activity,
        question: QuizQuestion,
        textView: WebView,
        imageView: ImageView,
        sourceView: TextView
    ) {
        clear(textView, imageView, sourceView)
        if (!question.hasStimulus()) return

        if (question.stimulusText.isNotBlank()) {
            RichContentRenderer.render(textView, question.stimulusText)
        }
        if (question.stimulusSource.isNotBlank()) {
            sourceView.visibility = View.VISIBLE
            sourceView.text = "Sumber: ${question.stimulusSource}"
        }
        if (!question.hasVisualStimulus()) return

        val localPath = question.stimulusImageLocalPath
        if (localPath.isNotBlank()) {
            val file = File(localPath)
            if (file.exists() && file.length() > 0) {
                decodeScaledFile(file.absolutePath, 1600, 1200)?.let {
                    imageView.setImageBitmap(it)
                    imageView.visibility = View.VISIBLE
                    return
                }
            }
        }
        val remoteUrl = question.stimulusImageUrl
        if (remoteUrl.isBlank()) return
        executor.execute {
            val bitmap = downloadBitmap(remoteUrl)
            activity.runOnUiThread {
                if (!activity.isFinishing && !activity.isDestroyed && bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    imageView.visibility = View.VISIBLE
                }
            }
        }
    }

    fun clear(textView: WebView, imageView: ImageView, sourceView: TextView) {
        RichContentRenderer.clear(textView)
        sourceView.text = ""
        sourceView.visibility = View.GONE
        imageView.setImageDrawable(null)
        imageView.visibility = View.GONE
    }

    private fun downloadBitmap(url: String): Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 12000
            connection.readTimeout = 20000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "CPNS-JUARA-Android")
            connection.connect()
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) { null }
        finally { connection?.disconnect() }
    }

    private fun decodeScaledFile(path: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > maxWidth * 2 || bounds.outHeight / sample > maxHeight * 2) sample *= 2
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        })
    }
}
