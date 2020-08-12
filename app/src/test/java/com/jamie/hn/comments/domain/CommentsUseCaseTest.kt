package com.jamie.hn.comments.domain

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.core.BaseTest
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.StoriesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Unconfined
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CommentsUseCaseTest : BaseTest() {

    @MockK
    private lateinit var repository: StoriesRepository

    @MockK
    private lateinit var story: Story

    @MockK
    private lateinit var onResult: (List<CommentWithDepth>) -> Unit

    private lateinit var commentsUseCase: CommentsUseCase
    private lateinit var scope: CoroutineScope

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { repository.story(any(), any()) } returns story

        commentsUseCase = CommentsUseCase(repository)
        scope = CoroutineScope(Unconfined)
    }

    @Test
    fun `when retrieveComments is called using cache true then fetch story from repository with using cache true and invoke callback`() {
        every { story.comments } returns emptyList()
        every { onResult.invoke(any()) } returns Unit

        commentsUseCase.retrieveComments(
            scope = scope,
            storyId = 1,
            useCache = true,
            onResult = onResult
        )

        coVerify { repository.story(1, true) }
        verify { onResult.invoke(any()) }
    }

    @Test
    fun `when retrieveComments is called using cache false then fetch story from repository with using cache false and invoke callback`() {
        every { story.comments } returns emptyList()
        every { onResult.invoke(any()) } returns Unit

        commentsUseCase.retrieveComments(
            scope = scope,
            storyId = 1,
            useCache = false,
            onResult = onResult
        )

        coVerify { repository.story(1, false) }
        verify { onResult.invoke(any()) }
    }

    @Nested
    inner class AllCommentsInChain {

        @Test
        fun `when the comment has no child comments then return a list containing only that comment with correct depth`() {
            val returnedComments = slot<List<CommentWithDepth>>()

            every { onResult.invoke(any()) } returns Unit
            coEvery { repository.story(any(), any()) } returns story(singleComment())

            commentsUseCase.retrieveComments(
                scope = scope,
                storyId = 1,
                useCache = false,
                onResult = onResult
            )

            verify { onResult.invoke(capture(returnedComments)) }

            assertEquals(1, returnedComments.captured.size)
            assertEquals(0, returnedComments.captured[0].depth)
            assertEquals(singleComment(), returnedComments.captured[0].comment)
        }

        @Test
        fun `when comment has multiple child comments then return the list containing those with their correct depth and removed their children`() {
            val returnedComments = slot<List<CommentWithDepth>>()

            every { onResult.invoke(any()) } returns Unit
            coEvery { repository.story(any(), any()) } returns story(singleCommentNestedComment())

            commentsUseCase.retrieveComments(
                scope = scope,
                storyId = 1,
                useCache = false,
                onResult = onResult
            )

            verify { onResult.invoke(capture(returnedComments)) }

            // We remove the nested child comments but keep the commentCount
            val firstComment = singleComment().copy(commentCount = 1)

            assertEquals(2, returnedComments.captured.size)
            assertEquals(0, returnedComments.captured[0].depth)
            assertEquals(firstComment, returnedComments.captured[0].comment)
            assertEquals(1, returnedComments.captured[1].depth)
            assertEquals(singleComment(), returnedComments.captured[1].comment)
        }
    }

    private fun story(comment: Comment) =
        Story(
            comments = listOf(comment),
            time = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
        )

    private fun singleCommentNestedComment() =
        singleComment().copy(comments = listOf(singleComment()), commentCount = 1)

    private fun singleComment() =
        Comment(
            author = "Jamie",
            comments = emptyList(),
            commentCount = 0,
            text = "text",
            time = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
        )
}
