package com.jamie.hn.articles.ui

import com.jamie.hn.articles.domain.Article
import java.time.*

class ArticleDataMapper(
    private val resourceProvider: ArticleResourceProvider
) {

    fun toArticleViewItem(article: Article): ArticleViewItem {
        return ArticleViewItem(
            article.by,
            comments(article.descendants),
            article.score.toString(),
            time(article.time),
            article.title,
            url(article.url)
        )
    }

    private fun comments(numComments: Int) = resourceProvider.comments(numComments)

    private fun time(time: Long): String {
        val timePost = LocalDateTime.ofInstant(Instant.ofEpochSecond(time), ZoneId.systemDefault())
        val timeNow = LocalDateTime.now()

        val timeBetween = Duration.between(timePost, timeNow)

        if (timeBetween.toDays() > 0) return "${timeBetween.toDays()}${resourceProvider.days}"
        if (timeBetween.toHours() > 0L) return "${timeBetween.toHours()}${resourceProvider.hours}"
        if (timeBetween.toMinutes() > 0L) return "${timeBetween.toMinutes()}${resourceProvider.minutes}"

        return ""
    }

    private fun url(url: String): String {
        if (url.isEmpty()) return ""

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

    private fun showLink(url: String) = url != ""
}