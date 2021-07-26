package com.jamie.hn.comments.domain

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.StoriesListType.ASK
import com.jamie.hn.core.StoriesListType.JOBS
import com.jamie.hn.core.StoriesListType.SHOW
import com.jamie.hn.core.StoriesListType.TOP
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.StoriesRepository
import com.jamie.hn.stories.repository.model.StoryResult
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
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CommentsUseCaseTest : BaseTest() {

    @MockK
    private lateinit var repository: StoriesRepository

    @MockK
    private lateinit var storyResult: StoryResult

    @MockK
    private lateinit var story: Story

    @MockK
    private lateinit var onResult: (List<CommentWithDepth>, Boolean, Boolean) -> Unit

    private lateinit var commentsUseCase: CommentsUseCase
    private lateinit var scope: CoroutineScope

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { repository.story(any(), any(), any(), any()) } returns storyResult
        every { storyResult.story } returns story
        every { storyResult.networkFailure } returns false

        commentsUseCase = CommentsUseCase(repository)
        scope = CoroutineScope(Unconfined)
    }

    @Test
    fun `when retrieveComments is called using cache true then fetch story from repository with cache true and requiring comments true and invoke callback`() {
        every { story.comments } returns emptyList()
        every { onResult.invoke(any(), any(), any()) } returns Unit

        runBlocking {
            commentsUseCase.retrieveComments(
                storyId = 1,
                useCache = true,
                onResult = onResult,
                requireComments = true,
                storiesListType = TOP
            )
        }

        coVerify {
            repository.story(
                id = 1,
                useCachedVersion = true,
                requireComments = true,
                storiesListType = TOP
            )
        }
        verify { onResult.invoke(any(), eq(false), eq(true)) }
    }

    @Test
    fun `when retrieveComments is called using cache false then fetch story from repository with cache false and invoke callback`() {
        every { story.comments } returns emptyList()
        every { onResult.invoke(any(), any(), any()) } returns Unit

        runBlocking {
            commentsUseCase.retrieveComments(
                storyId = 1,
                useCache = false,
                onResult = onResult,
                requireComments = true,
                storiesListType = ASK
            )
        }

        coVerify {
            repository.story(
                id = 1,
                useCachedVersion = false,
                requireComments = true,
                storiesListType = ASK
            )
        }
        verify { onResult.invoke(any(), eq(false), eq(false)) }
    }

    @Nested
    inner class AllCommentsInChain {

        @Test
        fun `when the comment has no child comments then return a list containing only that comment with correct depth`() {
            val returnedComments = slot<List<CommentWithDepth>>()

            every { onResult.invoke(any(), any(), any()) } returns Unit
            coEvery { repository.story(any(), any(), any(), any()) } returns StoryResult(
                story(
                    singleComment()
                )
            )

            runBlocking {
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = false,
                    onResult = onResult,
                    requireComments = true,
                    storiesListType = JOBS
                )
            }

            coVerify { repository.story(any(), any(), any(), eq(JOBS)) }
            verify { onResult.invoke(capture(returnedComments), any(), any()) }

            assertEquals(1, returnedComments.captured.size)
            assertEquals(0, returnedComments.captured[0].depth)
            assertEquals(singleComment(), returnedComments.captured[0].comment)
        }

        @Test
        fun `when comment has multiple child comments then return the list containing those with their correct depth and removed their children`() {
            val returnedComments = slot<List<CommentWithDepth>>()

            every { onResult.invoke(any(), any(), any()) } returns Unit
            coEvery { repository.story(any(), any(), any(), any()) } returns StoryResult(story(
                singleCommentNestedComment()
            ))

            runBlocking {
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = false,
                    onResult = onResult,
                    requireComments = true,
                    storiesListType = SHOW
                )
            }

            coVerify { repository.story(any(), any(), any(), eq(SHOW)) }
            verify { onResult.invoke(capture(returnedComments), any(), any()) }

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
