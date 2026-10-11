package com.example.localhtmlpreview

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import java.io.ByteArrayInputStream
import java.nio.file.Paths

class PreviewActivity : Activity() {
    private var server: LocalServer? = null
    @SuppressLint("SetJavaScriptEnabled") override fun onCreate(state: Bundle?) { super.onCreate(state)
        val root = Paths.get(intent.getStringExtra("root") ?: return finish()); val entry = intent.getStringExtra("entry") ?: "index.html"
        val web = WebView(this); web.settings.javaScriptEnabled = true; web.settings.domStorageEnabled = true; web.settings.allowFileAccess = false; web.settings.allowContentAccess = false; web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW; web.settings.javaScriptCanOpenWindowsAutomatically = false; web.settings.setSupportMultipleWindows(false)
        server = LocalServer(root); val url = server!!.start().replace("index.html", entry)
        val origin = url.substringBefore("/").let { url.substringBefore("/", it.length + 1) }
        web.webViewClient = object : WebViewClient() { override fun shouldOverrideUrlLoading(v: WebView, r: WebResourceRequest): Boolean { val u = r.url.toString(); if (u.startsWith("http://127.0.0.1")) return false; Toast.makeText(this@PreviewActivity, "已阻止外部链接", Toast.LENGTH_SHORT).show(); return true }; override fun shouldInterceptRequest(v: WebView, r: WebResourceRequest): WebResourceResponse? { if (!r.url.toString().startsWith("http://127.0.0.1")) return WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", emptyMap(), ByteArrayInputStream(ByteArray(0))); return super.shouldInterceptRequest(v, r) } }
        web.webChromeClient = object : WebChromeClient() { override fun onPermissionRequest(r: PermissionRequest) { r.deny() } }
        setContentView(web); web.loadUrl(url)
    }
    override fun onDestroy() { server?.stop(); server = null; super.onDestroy() }
}
