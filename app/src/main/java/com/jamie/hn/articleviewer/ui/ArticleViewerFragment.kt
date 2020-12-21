package com.jamie.hn.articleviewer.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.jamie.hn.R
import com.jamie.hn.core.extensions.visibleOrGone
import com.jamie.hn.databinding.ArticleViewerFragmentBinding

class ArticleViewerFragment : Fragment(R.layout.article_viewer_fragment) {

    private val url: String
        get() = arguments?.get("url") as String

    private var binding: ArticleViewerFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ArticleViewerFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use ChromeClient for determinate progress in our UI loader
        binding?.let {
            it.articleWebPage.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)

                    if (doNotUpdateUI()) return
                    it.progressBar.progress = newProgress
                }
            }

            it.articleWebPage.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)

                    if (doNotUpdateUI()) return
                    it.progressBar.visibleOrGone(true)
                    it.articleWebPage.visibleOrGone(false)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    if (doNotUpdateUI()) return
                    it.progressBar.visibleOrGone(false)
                    it.articleWebPage.visibleOrGone(true)
                }
            }

            it.articleWebPage.settings.apply {
                javaScriptEnabled = true
                builtInZoomControls = true
                useWideViewPort = true
                loadWithOverviewMode = true
            }

            it.articleWebPage.loadUrl(url)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    // If the user navigates back before the page has started/finished loading, ignore UI updates
    private fun doNotUpdateUI(): Boolean {
        binding?.let {
            return it.progressBar == null || it.articleWebPage == null
        }
        return true
    }
}
