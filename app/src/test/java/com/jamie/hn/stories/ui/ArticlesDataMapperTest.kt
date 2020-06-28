package com.jamie.hn.stories.ui

import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.core.ui.CoreDataMapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

class ArticlesDataMapperTest {

    @MockK
    private lateinit var storyResourceProvider: StoryResourceProvider
    @MockK
    private lateinit var coreDataMapper: CoreDataMapper

    private lateinit var storyDataMapper: StoryDataMapper

    @BeforeEach
    private fun setup() {
        MockKAnnotations.init(this)

        every { storyResourceProvider.comments(any()) } returns "1 comment"
        every { coreDataMapper.time(any()) } returns "1d"
        storyDataMapper = StoryDataMapper(coreDataMapper, storyResourceProvider)
    }

    @Test
    fun `when by is populated then populate Author`() {
        val article = Story(by = "Jamie")

        val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)

        assertEquals("Jamie", item.author)
    }

    @Test
    fun `when descendants is populated then populate Author`() {
        val article = Story(descendants = 1)

        val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)

        assertEquals("1 comment", item.comments)
    }

    @Test
    fun `when score is populated then populate score`() {
        val article = Story(score = 1)

        val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)

        assertEquals("1", item.score)
    }

    @Test
    fun `when title is populated then populate title`() {
        val article = Story(title = "Jamie")

        val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)

        assertEquals("Jamie", item.title)
    }

    @Test
    fun `when time is populated then use coreDataMapper time`() {
        val dateYesterday = LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)
        val article =
            Story(time = dateYesterday)

        val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)

        verify { coreDataMapper.time(dateYesterday) }
        assertEquals("1d", item.time)
    }

    @Nested
    inner class URL {

        @Test
        fun `when url is empty then return empty string`() {
            val article = Story(url = "")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("", item.url)
        }

        @Test
        fun `when url is null then return empty string`() {
            val article = Story(url = "")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("", item.url)
        }

        @Test
        fun `when url has http protocol then it is removed`() {
            val article =
                Story(url = "http://ddg.gg")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has https protocol then it is removed`() {
            val article =
                Story(url = "https://ddg.gg")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has a path then path is removed`() {
            val article =
                Story(url = "ddg.gg/jamie")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has https protocol and path then protocol and path are removed`() {
            val article =
                Story(url = "https://ddg.gg/page")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www then www is removed`() {
            val article =
                Story(url = "www.ddg.gg")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www and protocol then www and protocol are removed`() {
            val article =
                Story(url = "https://www.ddg.gg")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www and path then www and path are removed`() {
            val article =
                Story(url = "www.ddg.gg/jamie")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }

        @Test
        fun `when url has www path and protocol then all are removed`() {
            val article =
                Story(url = "https://www.ddg.gg/jamie")

            val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)
            assertEquals("ddg.gg", item.url)
        }
    }

    @Test
    fun `when comments callback is passed in then commentsCallback is assigned to ArticleViewItem`() {
        val article = Story()

        val item = storyDataMapper.toStoryViewItem(article, ::testCommentsCallback, ::testArticleViewerCallback)

        assertEquals(item.commentsCallback, ::testCommentsCallback)
    }

    private fun testCommentsCallback(id: Long) {
        println(id)
    }

    private fun testArticleViewerCallback(id: Long) {
        println(id)
    }
}
