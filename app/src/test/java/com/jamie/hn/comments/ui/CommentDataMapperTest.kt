package com.jamie.hn.comments.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
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

    private lateinit var commentDataMapper: CommentDataMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { coreDataMapper.time(any()) } returns "1d"
        commentDataMapper = spyk(CommentDataMapper(coreDataMapper))
    }

    @Test
    fun `when toCommentViewItem is called then correctly map basic fields`() {
        every { commentDataMapper.htmlTextParser("text") } returns "text"
        val commentWithDepth = CommentWithDepth(
            comment = Comment(
                author = "author",
                text = "text",
                time = DateTime.now(),
                commentCount = 0
            ),
            depth = 2
        )

        val commentViewItem = commentDataMapper.toCommentViewItem(commentWithDepth)

        assertEquals("author", commentViewItem.author)
        assertEquals("text", commentViewItem.text)
        assertEquals("1d", commentViewItem.time)
        assertEquals(2, commentViewItem.depth)
    }

    @Nested
    inner class Text {

        @Test
        fun `when text is empty on comment passed in then text is empty on comment returned`() {
            every { commentDataMapper.htmlTextParser("") } returns ""

            val commentWithDepth = CommentWithDepth(
                comment = Comment(
                    author = "author",
                    text = "",
                    time = DateTime.now(),
                    commentCount = 0
                ),
                depth = 2
            )

            val commentViewItem = commentDataMapper.toCommentViewItem(commentWithDepth)

            assertEquals("", commentViewItem.text)
        }

        @Test
        fun `when text does not in blank lines then return text correctly`() {
            every { commentDataMapper.htmlTextParser("text") } returns "text"

            val commentWithDepth = CommentWithDepth(
                comment = Comment(
                    author = "author",
                    text = "text",
                    time = DateTime.now(),
                    commentCount = 0
                ),
                depth = 2
            )

            val commentViewItem = commentDataMapper.toCommentViewItem(commentWithDepth)

            assertEquals("text", commentViewItem.text)
        }

        @Test
        fun `when text does end in 2 blank line then return the text without the 2 blank line`() {
            every { commentDataMapper.htmlTextParser("text\n\n") } returns "text\n\n"

            val commentWithDepth = CommentWithDepth(
                comment = Comment(
                    author = "author",
                    text = "text\n\n",
                    time = DateTime.now(),
                    commentCount = 0
                ),
                depth = 2
            )

            val commentViewItem = commentDataMapper.toCommentViewItem(commentWithDepth)

            assertEquals("text", commentViewItem.text)
        }
    }

    @Nested
    inner class ShowTopDivider {

        @Test
        fun `when depth is 0 then showTopDivider is true`() {
            every { commentDataMapper.htmlTextParser("text") } returns "text"

            val commentWithDepth = CommentWithDepth(
                comment = Comment(
                    author = "author",
                    text = "text",
                    time = DateTime.now(),
                    commentCount = 0
                ),
                depth = 0
            )

            val commentViewItem = commentDataMapper.toCommentViewItem(commentWithDepth)

            assertEquals(true, commentViewItem.showTopDivider)
        }

        @Test
        fun `when depth is more than 0 then showTopDivider is false`() {
            every { commentDataMapper.htmlTextParser("text") } returns "text"

            val commentWithDepth = CommentWithDepth(
                comment = Comment(
                    author = "author",
                    text = "text",
                    time = DateTime.now(),
                    commentCount = 0
                ),
                depth = 2
            )

            val commentViewItem = commentDataMapper.toCommentViewItem(commentWithDepth)

            assertEquals(false, commentViewItem.showTopDivider)
        }
    }
}
