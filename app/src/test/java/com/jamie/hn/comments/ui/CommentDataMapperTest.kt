package com.jamie.hn.comments.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.ui.CoreDataMapper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
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

    private lateinit var commentDataMapper: CommentDataMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        commentDataMapper = spyk(CommentDataMapper(commentsResourceProvider, coreDataMapper))

        every { coreDataMapper.time(any()) } returns "1d"
        every { commentsResourceProvider.children() } returns "children"
        every { commentDataMapper.processText(any(), any()) } returns mockk()
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
        assertEquals(collapseCallback, commentViewItem.longClickCommentListener)
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
}
