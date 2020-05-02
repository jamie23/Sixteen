package com.jamie.hn.articles.ui

import com.jamie.hn.articles.domain.Article
import com.jamie.hn.core.ui.CoreDataMapper

class ArticleDataMapper(
    private val coreDataMapper: CoreDataMapper,
    private val resourceProvider: ArticleResourceProvider
) {

    fun toArticleViewItem(article: Article, commentsCallback: (Long) -> Unit, articleViewerCallback: (Long) -> Unit): ArticleViewItem {
        return ArticleViewItem(
            article.id,
            article.by,
            comments(article.descendants),
            article.score.toString(),
            coreDataMapper.time(article.time),
            article.title,
            url(article.url),
            commentsCallback,
            articleViewerCallback
        )
    }

    private fun comments(numComments: Int) = resourceProvider.comments(numComments)

    private fun url(url: String?): String {
        if (url.isNullOrEmpty()) return ""

        return removeWWW(removePath(removeProtocol(url)))
    }

    private fun removeProtocol(url: String): String {
        val indexStart = url.indexOf("//")

        if (indexStart == -1) return url

        return url.substring(indexStart + 2, url.length)
    }

    private fun removePath(url: String): String {
        val indexPath = url.indexOf("/")

        if (indexPath == -1) return url

        return url.substring(0, indexPath)
    }

    private fun removeWWW(url: String): String {
        val indexWWW = url.indexOf("www")
        if (indexWWW == -1) return url

        return url.substring(4, url.length)
    }
}
