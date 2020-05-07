package com.jamie.hn.articleviewer.ui

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.jamie.hn.R
import kotlinx.android.synthetic.main.article_viewer_fragment.*

class ArticleViewerFragment : Fragment(R.layout.article_viewer_fragment) {
    private val url: String
        get() = arguments?.get("url") as String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        articleWebPage.webViewClient = WebViewClient()
        articleWebPage.settings.javaScriptEnabled = true
        articleWebPage.settings.builtInZoomControls = true
        articleWebPage.settings.useWideViewPort = true
        articleWebPage.settings.loadWithOverviewMode = true

        articleWebPage.loadUrl(url)
    }
}
