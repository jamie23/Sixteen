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
    private lateinit var commentsResourceProvider: CommentsResourceProvider

    private lateinit var commentDataMapper: CommentDataMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coreDataMapper.time(any()) } returns "1d"
        every { commentsResourceProvider.children() } returns "children"

        commentDataMapper = spyk(CommentDataMapper(commentsResourceProvider, coreDataMapper))
    }

    @Test
    fun `when toCommentViewItem is called then correctly map basic fields`() {
        every { commentDataMapper.htmlTextParser("") } returns "text"
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
            commentDataMapper.toCommentViewItem(commentCurrentState, collapseCallback)

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
        fun `when text is empty on comment passed in then text is empty on comment returned`() {
            every { commentDataMapper.htmlTextParser("") } returns ""

            val commentWithDepth = CommentCurrentState(
                CommentWithDepth(
                    comment = Comment(
                        author = "author",
                        commentCount = 0,
                        text = "",
                        time = DateTime.now()
                    ),
                    depth = 2,
                    id = 1
                ),
                state = FULL
            )

            val commentViewItem =
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback)

            assertEquals("", commentViewItem.text)
        }

        @Test
        fun `when text does not in blank lines then return text correctly`() {
            every { commentDataMapper.htmlTextParser("text") } returns "text"

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
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback)

            assertEquals("text", commentViewItem.text)
        }

        @Test
        fun `when text does end in 2 blank line then return the text without the 2 blank line`() {
            every { commentDataMapper.htmlTextParser("text\n\n") } returns "text\n\n"

            val commentWithDepth = CommentCurrentState(
                CommentWithDepth(
                    comment = Comment(
                        author = "author",
                        commentCount = 0,
                        text = "text\n\n",
                        time = DateTime.now()
                    ),
                    depth = 2,
                    id = 1
                ),
                state = FULL
            )

            val commentViewItem =
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback)

            assertEquals("text", commentViewItem.text)
        }
    }

    @Nested
    inner class ShowTopDivider {

        @Test
        fun `when depth is 0 then showTopDivider is true`() {
            every { commentDataMapper.htmlTextParser("text") } returns "text"

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
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback)

            assertEquals(true, commentViewItem.showTopDivider)
        }

        @Test
        fun `when depth is more than 0 then showTopDivider is false`() {
            every { commentDataMapper.htmlTextParser("text") } returns "text"

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
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback)

            assertEquals(false, commentViewItem.showTopDivider)
        }
    }

    @Nested
    inner class AuthorAndHiddenChildren {

        @Test
        fun `when toCommentView is called then correctly map authorAndHiddenChildren`() {
            every { commentDataMapper.htmlTextParser("text") } returns "text"

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
                commentDataMapper.toCommentViewItem(commentWithDepth, collapseCallback)

            assertEquals(commentViewItem.authorAndHiddenChildren, "author [0 children]")
        }
    }
}
