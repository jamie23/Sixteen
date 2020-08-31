package com.jamie.hn.comments.ui

import androidx.lifecycle.Observer
import com.jamie.hn.comments.domain.CommentsUseCase
import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.CommentsListViewModel.ListViewState
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.COLLAPSED
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.Event
import com.jamie.hn.core.InstantExecutorExtension
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
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
        fun `when init is called then we emit refreshing view state and retrieve the comments from the use case with the correct story id and use cache is true`() {
            coEvery { commentsUseCase.retrieveComments(any(), any(), any(), any()) } just Runs
            val observer = spyk<Observer<ListViewState>>()

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.init()

            coVerifySequence {
                observer.onChanged(ListViewState(emptyList(), true))
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = true,
                    onResult = any(),
                    requireComments = true
                )
            }
        }

        @Test
        fun `when the repository is populated from the use case then the callback modifies the commentsViewRepository with a CommentCurrentState with the id as index and state as full`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsToMapper),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init()

            callback.invoke(useCaseResponse(), false, false)

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
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val mockedCommentViewItem = mockk<CommentViewItem>()
            val observer = spyk<Observer<ListViewState>>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any())
            } returns mockedCommentViewItem

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.init()

            callback.invoke(useCaseResponse(), false, false)
            verify {
                observer.onChanged(
                    ListViewState(
                        listOf(
                            mockedCommentViewItem,
                            mockedCommentViewItem
                        ), false
                    )
                )
            }
        }

        @Test
        fun `when the repository is populated from the use case with network failure as true and no items then emit an event for network failure with no cache`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val observerErrorNoCacheResults = spyk<Observer<Event<Unit>>>()
            val observerErrorCachedResults = spyk<Observer<Event<Unit>>>()

            commentsListViewModel.networkErrorNoCacheResults()
                .observeForever(observerErrorNoCacheResults)
            commentsListViewModel.networkErrorCachedResults()
                .observeForever(observerErrorCachedResults)

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    any(),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init()

            callback.invoke(emptyList(), true, false)

            verify(exactly = 1) { observerErrorNoCacheResults.onChanged(any()) }
            verify(exactly = 0) { observerErrorCachedResults.onChanged(any()) }
        }

        @Test
        fun `when the repository is populated from the use case with network failure as true but still has items and not using cache then emit an event for network failure with cached results and update view state`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val observerErrorNoCacheResults = spyk<Observer<Event<Unit>>>()
            val observerErrorCachedResults = spyk<Observer<Event<Unit>>>()
            val observerViewState = spyk<Observer<ListViewState>>()
            val mockedCommentViewItem = mockk<CommentViewItem>()

            commentsListViewModel.networkErrorNoCacheResults()
                .observeForever(observerErrorNoCacheResults)
            commentsListViewModel.networkErrorCachedResults()
                .observeForever(observerErrorCachedResults)
            commentsListViewModel.commentsViewState().observeForever(observerViewState)

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any())
            } returns mockedCommentViewItem

            commentsListViewModel.init()

            callback.invoke(useCaseResponse(), true, false)

            verify(exactly = 1) { observerErrorCachedResults.onChanged(any()) }
            verify(exactly = 0) { observerErrorNoCacheResults.onChanged(any()) }

            verify {
                observerViewState.onChanged(
                    ListViewState(
                        listOf(
                            mockedCommentViewItem,
                            mockedCommentViewItem
                        ), false
                    )
                )
            }
        }

        @Test
        fun `when the repository is populated from the use case with network failure as true but still has items and is using cache then do not emit an event for network failure with cached results but do update view state`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val observerErrorNoCacheResults = spyk<Observer<Event<Unit>>>()
            val observerErrorCachedResults = spyk<Observer<Event<Unit>>>()
            val observerViewState = spyk<Observer<ListViewState>>()
            val mockedCommentViewItem = mockk<CommentViewItem>()

            commentsListViewModel.networkErrorNoCacheResults()
                .observeForever(observerErrorNoCacheResults)
            commentsListViewModel.networkErrorCachedResults()
                .observeForever(observerErrorCachedResults)
            commentsListViewModel.commentsViewState().observeForever(observerViewState)

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any())
            } returns mockedCommentViewItem

            commentsListViewModel.init()

            callback.invoke(useCaseResponse(), false, false)

            verify(exactly = 0) { observerErrorCachedResults.onChanged(any()) }
            verify(exactly = 0) { observerErrorNoCacheResults.onChanged(any()) }

            verify {
                observerViewState.onChanged(
                    ListViewState(
                        listOf(
                            mockedCommentViewItem,
                            mockedCommentViewItem
                        ), false
                    )
                )
            }
        }
    }

    @Nested
    inner class LongClickListener {

        @Test
        fun `when comment state is full then set the state to be collapsed`() {
            val longClickListenerCallback = slot<(Int) -> Unit>()
            val commentsUseCaseCallback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(commentsUseCaseCallback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback)
                )
            } returns mockk()

            commentsListViewModel.init()

            commentsUseCaseCallback.invoke(useCaseResponse(), false, false)
            longClickListenerCallback.invoke(0)

            assertEquals(COLLAPSED, commentsPassedToMapper[2].state)
        }

        @Test
        fun `when comment state is collapsed then set the state to be full`() {
            val longClickListenerCallback = slot<(Int) -> Unit>()
            val commentsUseCaseCallback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(commentsUseCaseCallback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback)
                )
            } returns mockk()

            commentsListViewModel.init()

            commentsUseCaseCallback.invoke(useCaseResponse(), false, false)
            longClickListenerCallback.invoke(0)

            assertEquals(COLLAPSED, commentsPassedToMapper[2].state)

            longClickListenerCallback.invoke(0)

            assertEquals(FULL, commentsPassedToMapper[4].state)
        }

        // When the comment with state's id is the lastIndex then update nothing else
        @Test
        fun `when comment is long clicked and is the last in the list then do not update any other children`() {
            val longClickListenerCallback = slot<(Int) -> Unit>()
            val commentsUseCaseCallback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(commentsUseCaseCallback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback)
                )
            } returns mockk()

            commentsListViewModel.init()

            commentsUseCaseCallback.invoke(useCaseResponseWithChildren(), false, false)

            assertEquals(FULL, commentsPassedToMapper[0].state)
            assertEquals(FULL, commentsPassedToMapper[1].state)

            longClickListenerCallback.invoke(1)

            assertEquals(FULL, commentsPassedToMapper[2].state)
            assertEquals(COLLAPSED, commentsPassedToMapper[3].state)
        }

        @Test
        fun `when comment is long clicked, is not the last index and there is a sibling later then filter out all children before next sibling`() {
            val longClickListenerCallback = slot<(Int) -> Unit>()
            val commentsUseCaseCallback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(commentsUseCaseCallback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback)
                )
            } returns mockk()

            commentsListViewModel.init()

            val list = useCaseResponseWithChildren()
            list.add(
                // Dennis here is not a child of the clicked comment
                CommentWithDepth(
                    comment = Comment(author = "Dennis", commentCount = 0, time = DateTime.now()),
                    depth = 0
                )
            )

            commentsUseCaseCallback.invoke(list, false, false)

            assertEquals(FULL, commentsPassedToMapper[0].state)
            assertEquals(FULL, commentsPassedToMapper[1].state)
            assertEquals(FULL, commentsPassedToMapper[2].state)

            longClickListenerCallback.invoke(0)

            assertEquals(5, commentsPassedToMapper.size)
            assertEquals(COLLAPSED, commentsPassedToMapper[3].state)
            assertEquals(0, commentsPassedToMapper[3].comment.id)
            assertEquals(FULL, commentsPassedToMapper[4].state)
            assertEquals(2, commentsPassedToMapper[4].comment.id)
        }

        @Test
        fun `when comment is long clicked, is not the last index and there is no sibling or lower depth comments then filter results until end of list`() {
            val longClickListenerCallback = slot<(Int) -> Unit>()
            val commentsUseCaseCallback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(commentsUseCaseCallback),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback)
                )
            } returns mockk()

            commentsListViewModel.init()

            val list = useCaseResponseWithChildren()
            list.add(
                // Dennis here is a child of the clicked comment
                CommentWithDepth(
                    comment = Comment(author = "Dennis", commentCount = 0, time = DateTime.now()),
                    depth = 1
                )
            )

            commentsUseCaseCallback.invoke(list, false, false)

            assertEquals(FULL, commentsPassedToMapper[0].state)
            assertEquals(FULL, commentsPassedToMapper[1].state)
            assertEquals(FULL, commentsPassedToMapper[2].state)

            longClickListenerCallback.invoke(0)

            assertEquals(4, commentsPassedToMapper.size)
            assertEquals(COLLAPSED, commentsPassedToMapper[3].state)
            assertEquals(0, commentsPassedToMapper[3].comment.id)
        }
    }

    @Test
    fun `when userManuallyRefreshed is called then retrieve the comments from the use case with use cache as false`() {
        coEvery { commentsUseCase.retrieveComments(any(), any(), any(), any()) } just Runs

        commentsListViewModel.userManuallyRefreshed()

        coVerify {
            commentsUseCase.retrieveComments(
                storyId = 1,
                useCache = false,
                onResult = any(),
                requireComments = true
            )
        }
    }

    private fun useCaseResponse() = mutableListOf(
        CommentWithDepth(
            comment = Comment(author = "Jamie", commentCount = 0, time = DateTime.now()),
            depth = 0
        ),
        CommentWithDepth(
            comment = Comment(author = "Alex", commentCount = 0, time = DateTime.now()),
            depth = 0
        )
    )

    private fun useCaseResponseWithChildren() = mutableListOf(
        CommentWithDepth(
            comment = Comment(author = "Jamie", commentCount = 1, time = DateTime.now()),
            depth = 0
        ),
        CommentWithDepth(
            comment = Comment(author = "Alex", commentCount = 0, time = DateTime.now()),
            depth = 1
        )
    )
}
