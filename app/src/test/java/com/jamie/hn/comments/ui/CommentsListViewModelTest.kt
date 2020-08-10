package com.jamie.hn.comments.ui

import androidx.lifecycle.Observer
import com.jamie.hn.comments.domain.CommentsUseCase
import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.CommentsListViewModel.ListViewState
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.comments.ui.repository.model.CurrentState.HIDDEN
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.InstantExecutorExtension
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.StoriesRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class CommentsListViewModelTest : BaseTest() {

    @MockK
    private lateinit var commentDataMapper: CommentDataMapper

    @MockK
    private lateinit var commentsUseCase: CommentsUseCase

    private lateinit var commentsListViewModel: CommentsListViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        commentsListViewModel = CommentsListViewModel(
            commentDataMapper = commentDataMapper,
            storyId = 1,
            commentsUseCase = commentsUseCase
        )
    }

    @Nested
    inner class Init {

        @Test
        fun `when init is called then we emit refreshing view state and retrieve the comments from the use with the correct story id`() {
            coEvery { commentsUseCase.retrieveComments(any(), any(), any(), any()) } just Runs
            val observer = spyk<Observer<ListViewState>>()

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.init()

            coVerifySequence {
                observer.onChanged(ListViewState(emptyList(), true))
                commentsUseCase.retrieveComments(any(), 1, false, any())
            }
        }

        @Test
        fun `when the repository is populated from the use case then the callback modifies the commentsViewRepository with a CommentCurrentState with the id as index and state as full`() {
            val callback = slot<(List<CommentWithDepth>) -> Unit>()
            val commentsToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    capture(callback)
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsToMapper),
                    any()
                )
            } returns mockk()

            val useCaseResponse = useCaseResponse()

            commentsListViewModel.init()

            callback.invoke(useCaseResponse)

            assertEquals(2, commentsToMapper.size)

            assertEquals(0, commentsToMapper[0].comment.id)
            assertEquals("Jamie", commentsToMapper[0].comment.comment.author)
            assertEquals(FULL, commentsToMapper[0].state)

            assertEquals(1, commentsToMapper[1].comment.id)
            assertEquals("Alex", commentsToMapper[1].comment.comment.author)
            assertEquals(FULL, commentsToMapper[1].state)
        }

        @Test
        fun `when the repository is populated from the use case, we map the comments from the repository, mapping via the mapper and posting the value with refreshing false`() {
            val callback = slot<(List<CommentWithDepth>) -> Unit>()
            val mockedCommentViewItem = mockk<CommentViewItem>()
            val observer = spyk<Observer<ListViewState>>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    capture(callback)
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any())
            } returns mockedCommentViewItem

            val useCaseResponse = useCaseResponse()

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.init()

            callback.invoke(useCaseResponse)
            verify {
                observer.onChanged(ListViewState(listOf(mockedCommentViewItem, mockedCommentViewItem), false))
            }
        }
    }

    @Nested
    inner class LongClickListener {
        // Clicked on comment when full is set to collapsed
        // Clicked on comment when collapsed is set to full

        // When the comment with state's id is the lastIndex then update nothing else

        // When the comment with states id is not the lastIndex
            // when the id of next sibling is null (there is no next sibling, then update til end of list
            // when the id of next sibling is not null then update until the sibling
    }

    private fun useCaseResponse() = listOf(
        CommentWithDepth(
            comment = Comment(author = "Jamie", commentCount = 0, time = DateTime.now()),
            depth = 0
        ),
        CommentWithDepth(
            comment = Comment(author = "Alex", commentCount = 0, time = DateTime.now()),
            depth = 0
        )
    )
}
