package com.jamie.hn.stories.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.core.ui.CoreDataMapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StoriesDataMapperTest {

    @MockK
    private lateinit var storyResourceProvider: StoryResourceProvider

    @MockK
    private lateinit var coreDataMapper: CoreDataMapper

    private lateinit var storyDataMapper: StoryDataMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { storyResourceProvider.comments(any()) } returns "1 comment"
        every { storyResourceProvider.score(any()) } returns "point"
        every { coreDataMapper.time(any()) } returns "1d"
        storyDataMapper = StoryDataMapper(coreDataMapper, storyResourceProvider)
    }

    @Test
    fun `when toStoryViewItem is called then correctly map fields`() {
        val comments = listOf(Comment(time = DateTime.now(), commentCount = 0))
        val story = Story(
            author = "Jamie",
            comments = comments,
            commentCount = 1,
            commentsUrl = "commentsUrl",
            domain = "domain",
            id = 2,
            score = 3,
            time = DateTime.now(),
            title = "title",
            url = "url"
        )

        val commentsCallback = mockk<(Int) -> Unit>()
        val storyViewerCallback = mockk<(Int) -> Unit>()

        val storyViewItem = storyDataMapper.toStoryViewItem(
            story,
            commentsCallback,
            storyViewerCallback
        )

        assertEquals(2, storyViewItem.id)
        assertEquals("1 comment", storyViewItem.comments)
        assertEquals("Jamie", storyViewItem.author)
        assertEquals("3", storyViewItem.score)
        assertEquals("point", storyViewItem.scoreText)
        assertEquals("1d", storyViewItem.time)
        assertEquals("title", storyViewItem.title)
        assertEquals("domain", storyViewItem.url)
        assertEquals(commentsCallback, storyViewItem.commentsCallback)
        assertEquals(storyViewItem.storyViewerCallback, storyViewItem.storyViewerCallback)
    }
}
