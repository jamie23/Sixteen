package com.jamie.hn.articles.ui

import com.jamie.hn.articles.domain.Article
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class ArticlesDataMapperTest {

    @MockK
    private lateinit var articleResourceProvider: ArticleResourceProvider

    private lateinit var articleDataMapper: ArticleDataMapper

    @BeforeEach
    private fun setup() {
        MockKAnnotations.init(this)

        every { articleResourceProvider.days } returns "d"
        every { articleResourceProvider.hours } returns "h"
        every { articleResourceProvider.minutes } returns "m"
        every { articleResourceProvider.comments(any()) } returns "1 comment"

        articleDataMapper = ArticleDataMapper(articleResourceProvider)
    }

    @Test
    fun `when by is populated then populate Author`() {
        val article = Article(by = "Jamie")

        val item = articleDataMapper.toArticleViewItem(article)

        assertEquals("Jamie", item.author)
    }

    @Test
    fun `when descendants is populated then populate Author`() {
        val article = Article(descendants = 1)

        val item = articleDataMapper.toArticleViewItem(article)

        assertEquals("1 comment", item.comments)
    }

    @Test
    fun `when score is populated then populate score`() {
        val article = Article(score = 1)

        val item = articleDataMapper.toArticleViewItem(article)

        assertEquals("1", item.score)
    }

    @Test
    fun `when title is populated then populate title`() {
        val article = Article(title = "Jamie")

        val item = articleDataMapper.toArticleViewItem(article)

        assertEquals("Jamie", item.title)
    }

    @Nested
    inner class Time {

        @Test
        fun `when time is yesterday then use days`() {
            val dateYesterday = LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)
            val article = Article(time = dateYesterday)

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("1d", item.time)
        }

        @Test
        fun `when time is hour ago then use hours`() {
            val dateHour = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC)
            val article = Article(time = dateHour)

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("1h", item.time)
        }

        @Test
        fun `when time is minutes ago then use minutes`() {
            val dateMinute = LocalDateTime.now().minusMinutes(1).toEpochSecond(ZoneOffset.UTC)
            val article = Article(time = dateMinute)

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("1m", item.time)
        }

        @Test
        fun `when time is outside of checked range then use empty string`() {
            val dateNow = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
            val article = Article(time = dateNow)

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("", item.time)
        }
    }

    @Nested
    inner class URL {

        @Test
        fun `when url is empty then return empty string`() {
            val article = Article(url = "")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("", item.url)
        }

        @Test
        fun `when url has http protocol then it is removed`() {
            val article = Article(url = "http://ddg.gg")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has https protocol then it is removed`() {
            val article = Article(url = "https://ddg.gg")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has a path then path is removed`() {
            val article = Article(url = "ddg.gg/jamie")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has https protocol and path then protocol and path are removed`() {
            val article = Article(url = "https://ddg.gg/page")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www then www is removed`() {
            val article = Article(url = "www.ddg.gg")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www and protocol then www and protocol are removed`() {
            val article = Article(url = "https://www.ddg.gg")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www and path then www and path are removed`() {
            val article = Article(url = "www.ddg.gg/jamie")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www path and protocol then all are removed`() {
            val article = Article(url = "https://www.ddg.gg/jamie")

            val item = articleDataMapper.toArticleViewItem(article)
            assertEquals("ddg.gg", item.url)
        }
    }
}
