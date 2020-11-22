package com.jamie.hn.comments.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.comments.ui.repository.model.CurrentState.HEADER
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.ui.CoreDataMapper
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.ui.StoryResourceProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CommentDataMapperTest : BaseTest() {

    @MockK
    private lateinit var coreDataMapper: CoreDataMapper

    @MockK
    private lateinit var collapseCallback: (Int) -> Unit

    @MockK
    private lateinit var urlClickedCallback: (String) -> Unit

    @MockK
    private lateinit var commentsResourceProvider: CommentsResourceProvider

    @MockK
    private lateinit var resourceProvider: StoryResourceProvider

    private lateinit var commentDataMapper: CommentDataMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        commentDataMapper = spyk(CommentDataMapper(commentsResourceProvider, coreDataMapper, resourceProvider))

        every { coreDataMapper.time(any()) } returns "1d"
        every { commentsResourceProvider.children() } returns "children"
        every { commentDataMapper.processText(any(), any()) } returns mockk()
        every { resourceProvider.comments(any()) } returns "1 comment"
        every { resourceProvider.score(any()) } returns "point"
    }

    @Test
    fun `when toCommentViewItem is called then correctly map basic fields`() {
        val commentCurrentState = CommentCurrentState(
            CommentWithDepth(
                comment = Comment(
                    author = "author",
                    time = DateTime.now(),
                    commentCount = 0
                ),
                depth = 2,
                id = 1
            ),
            state = FULL
        )

        val commentViewItem =
            commentDataMapper.toCommentViewItem(
                commentCurrentState,
                collapseCallback,
                urlClickedCallback
            )

        assertEquals("author", commentViewItem.author)
        assertEquals("1d", commentViewItem.time)
        assertEquals(2, commentViewItem.depth)
        assertEquals(collapseCallback, commentViewItem.clickCommentListener)
        assertEquals(1, commentViewItem.id)
        assertEquals(FULL, commentViewItem.state)
    }

    @Nested
    inner class Text {
        @Test
        fun `when we map the text then we use the return value of processText`() {
            every {
                commentDataMapper.processText(
                    "text",
                    urlClickedCallback
                )
            } returns "returned chars"

            val commentWithDepth = CommentCurrentState(
                CommentWithDepth(
                    comment = Comment(
                        author = "author",
                        commentCount = 0,
                        text = "text",
                        time = DateTime.now()
                    ),
                    depth = 0,
                    id = 1
                ),
                state = FULL
            )

            val commentViewItem =
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback, urlClickedCallback)

            assertEquals("returned chars", commentViewItem.text)
        }
    }

    @Nested
    inner class ShowTopDivider {

        @Test
        fun `when depth is 0 then showTopDivider is true`() {
            val commentWithDepth = CommentCurrentState(
                CommentWithDepth(
                    comment = Comment(
                        author = "author",
                        commentCount = 0,
                        text = "text",
                        time = DateTime.now()
                    ),
                    depth = 0,
                    id = 1
                ),
                state = FULL
            )

            val commentViewItem =
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback, urlClickedCallback)

            assertEquals(true, commentViewItem.showTopDivider)
        }

        @Test
        fun `when depth is more than 0 then showTopDivider is false`() {
            val commentWithDepth = CommentCurrentState(
                CommentWithDepth(
                    comment = Comment(
                        author = "author",
                        commentCount = 0,
                        text = "text",
                        time = DateTime.now()
                    ),
                    depth = 2,
                    id = 1
                ),
                state = FULL
            )

            val commentViewItem =
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback, urlClickedCallback)

            assertEquals(false, commentViewItem.showTopDivider)
        }
    }

    @Nested
    inner class AuthorAndHiddenChildren {

        @Test
        fun `when toCommentView is called then correctly map authorAndHiddenChildren`() {
            val commentWithDepth = CommentCurrentState(
                CommentWithDepth(
                    comment = Comment(
                        author = "author",
                        commentCount = 0,
                        text = "text",
                        time = DateTime.now()
                    ),
                    depth = 2,
                    id = 1
                ),
                state = FULL
            )

            val commentViewItem =
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback, urlClickedCallback)

            assertEquals(commentViewItem.authorAndHiddenChildren, "author [0 children]")
        }
    }

    @Test
    fun `when toStoryHeaderViewItem is called then correctly map basic fields`() {
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
            url = "url",
            serverSortedOrder = 0
        )
        val storyViewerCallback = mockk<() -> Unit>()

        val headerViewItem =
            commentDataMapper.toStoryHeaderViewItem(
                story,
                storyViewerCallback
            )

        assertEquals(2, headerViewItem.id)
        assertEquals(HEADER, headerViewItem.state)
        assertEquals("Jamie", headerViewItem.author)
        assertEquals("1d", headerViewItem.time)
        assertEquals("1 comment", headerViewItem.comments)
        assertEquals("3", headerViewItem.score)
        assertEquals("point", headerViewItem.scoreText)
        assertEquals("title", headerViewItem.title)
        assertEquals("domain", headerViewItem.url)
        assertEquals(storyViewerCallback, headerViewItem.storyViewerCallback)
    }
}
