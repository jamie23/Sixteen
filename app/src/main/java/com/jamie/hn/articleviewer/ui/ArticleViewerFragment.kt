package com.jamie.hn.articleviewer.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visibleOrGone
import kotlinx.android.synthetic.main.article_viewer_fragment.*

class ArticleViewerFragment : Fragment(R.layout.article_viewer_fragment) {
    private val url: String
        get() = arguments?.get("url") as String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use ChromeClient for determinate progress in our UI loader
        articleWebPage.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
            }
        }

        articleWebPage.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibleOrGone(true)
                articleWebPage.visibleOrGone(false)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibleOrGone(false)
                articleWebPage.visibleOrGone(true)
            }
        }

        articleWebPage.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        articleWebPage.loadUrl(url)
    }
}
