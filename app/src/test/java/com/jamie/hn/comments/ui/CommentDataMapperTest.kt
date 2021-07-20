package com.jamie.hn.comments.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.comments.ui.repository.model.CurrentState.HEADER
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.ui.CoreDataMapper
import com.jamie.hn.stories.domain.model.Story
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
    private lateinit var resourceProvider: CommentsResourceProvider

    private lateinit var commentDataMapper: CommentDataMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        commentDataMapper = spyk(CommentDataMapper(resourceProvider, coreDataMapper))

        every { coreDataMapper.time(any()) } returns "1d"
        every { commentDataMapper.processText(any(), any()) } returns mockk()
        every { resourceProvider.hidden() } returns "hidden"
        every { resourceProvider.numComments(any()) } returns "1 comment"
        every { resourceProvider.score(any()) } returns "point"
        every { resourceProvider.op() } returns "op"
    }

    @Nested
    inner class ToCommentViewItem {

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
                    "author",
                    collapseCallback,
                    urlClickedCallback
                )

            assertEquals("author", commentViewItem.author)
            assertEquals(true, commentViewItem.isOP)
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
                    commentDataMapper.toCommentViewItem(
                        commentWithDepth,
                        "",
                        collapseCallback,
                        urlClickedCallback
                    )

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
                    commentDataMapper.toCommentViewItem(
                        commentWithDepth,
                        "",
                        collapseCallback,
                        urlClickedCallback
                    )

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
                    commentDataMapper.toCommentViewItem(
                        commentWithDepth,
                        "",
                        collapseCallback,
                        urlClickedCallback
                    )

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
                    commentDataMapper.toCommentViewItem(
                        commentWithDepth,
                        "author op",
                        collapseCallback,
                        urlClickedCallback
                    )

                assertEquals(commentViewItem.authorAndHiddenChildren, "author [1 hidden]")
            }
        }
    }

    @Nested
    inner class ToStoryHeaderViewItem {

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
                text = ""
            )
            val storyViewerCallback = mockk<() -> Unit>()

            val headerViewItem =
                commentDataMapper.toStoryHeaderViewItem(
                    story,
                    urlClickedCallback,
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
            assertEquals(false, headerViewItem.showAskText)
            assertEquals(true, headerViewItem.showNavigateToArticle)
            assertEquals(storyViewerCallback, headerViewItem.storyViewerCallback)
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
                    text = "text"
                )

                val headerViewItem =
                    commentDataMapper.toStoryHeaderViewItem(
                        story,
                        urlClickedCallback,
                        mockk()
                    )

                assertEquals("returned chars", headerViewItem.text)
            }
        }

        @Nested
        inner class ShowAskText {

            @Test
            fun `when story is an askStory then showAskText is true`() {
                every {
                    commentDataMapper.processText(
                        "text",
                        urlClickedCallback
                    )
                } returns "returned chars"

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
                    title = "Ask HN:",
                    url = "url",
                    text = "text"
                )

                val headerViewItem =
                    commentDataMapper.toStoryHeaderViewItem(
                        story,
                        urlClickedCallback,
                        mockk()
                    )

                assertTrue(headerViewItem.showAskText)
            }
        }

        @Test
        fun `when story is not an askStory then showAskText is false`() {
            every {
                commentDataMapper.processText(
                    "text",
                    urlClickedCallback
                )
            } returns "returned chars"

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
                title = "This is not an Ask story",
                url = "url",
                text = "text"
            )

            val headerViewItem =
                commentDataMapper.toStoryHeaderViewItem(
                    story,
                    urlClickedCallback,
                    mockk()
                )

            assertFalse(headerViewItem.showAskText)
        }
    }

    @Nested
    inner class ShowNavigateToArticle {

        @Test
        fun `when story is an askStory then showNavigateToArticle is false`() {
            every {
                commentDataMapper.processText(
                    "text",
                    urlClickedCallback
                )
            } returns "returned chars"

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
                title = "Ask HN:",
                url = "url",
                text = "text"
            )

            val headerViewItem =
                commentDataMapper.toStoryHeaderViewItem(
                    story,
                    urlClickedCallback,
                    mockk()
                )

            assertFalse(headerViewItem.showNavigateToArticle)
        }

        @Test
        fun `when story is not an askStory then showNavigateToArticle is true`() {
            every {
                commentDataMapper.processText(
                    "text",
                    urlClickedCallback
                )
            } returns "returned chars"

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
                title = "This is not an Ask story",
                url = "url",
                text = "text"
            )

            val headerViewItem =
                commentDataMapper.toStoryHeaderViewItem(
                    story,
                    urlClickedCallback,
                    mockk()
                )

            assertTrue(headerViewItem.showNavigateToArticle)
        }
    }
}
