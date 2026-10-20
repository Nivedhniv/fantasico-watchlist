package com.example.dowatch

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Terms : AppCompatActivity() {

    companion object {
        const val EXTRA_PAGE = "page"
        const val PAGE_LEGAL = "legal.html"
        const val PAGE_HELP = "help.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_terms)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val back = findViewById<ImageButton>(R.id.Back)
        back.setOnClickListener {
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            back.translationY = systemBars.top.toFloat()
            insets
        }

        val page = intent.getStringExtra(EXTRA_PAGE) ?: PAGE_LEGAL
        title = if (page == PAGE_HELP) "Help" else "Terms & Privacy"

        val webView = findViewById<WebView>(R.id.policyWebView)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url
                return when (url.scheme) {
                    "mailto" -> {
                        try {
                            startActivity(Intent(Intent.ACTION_SENDTO, url))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(this@Terms, "No email app found", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    "http", "https" -> {
                        startActivity(Intent(Intent.ACTION_VIEW, url))
                        true
                    }
                    else -> false // file:// links (help <-> legal) stay in the WebView
                }
            }
        }
        webView.loadUrl("file:///android_asset/$page")
    }
}