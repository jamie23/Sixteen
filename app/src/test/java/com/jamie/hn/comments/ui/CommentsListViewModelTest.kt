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
import com.jamie.hn.core.StoriesListType.ASK
import com.jamie.hn.core.StoriesListType.JOBS
import com.jamie.hn.core.StoriesListType.NEW
import com.jamie.hn.core.StoriesListType.SHOW
import com.jamie.hn.core.StoriesListType.TOP
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.StoryResult
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.invoke
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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

    @MockK
    private lateinit var storiesUseCase: StoriesUseCase

    @MockK
    private lateinit var storyHeaderItem: HeaderViewItem

    @MockK
    private lateinit var commentsResourceProvider: CommentsResourceProvider

    private lateinit var commentsListViewModel: CommentsListViewModel

    private val story = Story(
        id = 23,
        time = DateTime.parse(
            "23/08/2020 09:00:00",
            DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        ),
        title = "title",
        url = "url"
    )
    private val storyResults = StoryResult(story)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { storiesUseCase.getStory(any(), any(), any()) } returns storyResults
        every { commentDataMapper.toStoryHeaderViewItem(any(), any()) } returns storyHeaderItem
        every { commentsResourceProvider.article() } returns "Article"
        every { commentsResourceProvider.comments() } returns "Comments"

        commentsListViewModel = CommentsListViewModel(
            commentDataMapper = commentDataMapper,
            storyId = 1,
            commentsUseCase = commentsUseCase,
            storiesUseCase = storiesUseCase,
            commentsResourceProvider = commentsResourceProvider
        )
        commentsListViewModel.storyListType = TOP
    }

    @Nested
    inner class Init {

        @Test
        fun `when init is called then we emit refreshing view state and retrieve the comments from the use case with the correct story id, use cache is true and correct storyListType`() {
            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs
            val observer = spyk<Observer<ListViewState>>()

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.init(storyListType, storyType)

            coVerifySequence {
                observer.onChanged(ListViewState(emptyList(), true))
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = true,
                    onResult = any(),
                    requireComments = true,
                    storiesListType = TOP
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsToMapper),
                    any(),
                    any()
                )
            } returns mockk()

            commentsListViewModel.storyListType = NEW
            commentsListViewModel.init(storyListType, storyType)

            callback.invoke(useCaseResponseWithoutChildren(), false, false)

            coVerify {
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = true,
                    onResult = any(),
                    requireComments = true,
                    storiesListType = NEW
                )
            }

            assertEquals(2, commentsToMapper.size)

            assertEquals(0, commentsToMapper[0].comment.id)
            assertEquals("Jamie", commentsToMapper[0].comment.comment.author)
            assertEquals(FULL, commentsToMapper[0].state)

            assertEquals(1, commentsToMapper[1].comment.id)
            assertEquals("Alex", commentsToMapper[1].comment.comment.author)
            assertEquals(FULL, commentsToMapper[1].state)
        }

        @Test
        fun `when the repository is populated from the use case, we map the comments from the repository, mapping via the mapper, adding the header and posting the value with refreshing false`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val mockedCommentViewItem = mockk<CommentViewItem>()
            val observer = spyk<Observer<ListViewState>>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any(), any())
            } returns mockedCommentViewItem

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.init(storyListType, storyType)

            callback.invoke(useCaseResponseWithoutChildren(), false, false)
            verify {
                observer.onChanged(
                    ListViewState(
                        listOf(
                            storyHeaderItem,
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    any(),
                    any(),
                    any()
                )
            } returns mockk()

            commentsListViewModel.storyListType = SHOW
            commentsListViewModel.init(storyListType, storyType)

            callback.invoke(emptyList(), true, false)

            coVerify {
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = true,
                    onResult = any(),
                    requireComments = true,
                    storiesListType = SHOW
                )
            }

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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any(), any())
            } returns mockedCommentViewItem

            commentsListViewModel.init(storyListType, storyType)

            callback.invoke(useCaseResponseWithoutChildren(), true, false)

            verify(exactly = 1) { observerErrorCachedResults.onChanged(any()) }
            verify(exactly = 0) { observerErrorNoCacheResults.onChanged(any()) }

            verify {
                observerViewState.onChanged(
                    ListViewState(
                        listOf(
                            storyHeaderItem,
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any(), any())
            } returns mockedCommentViewItem

            commentsListViewModel.init(storyListType, storyType)

            callback.invoke(useCaseResponseWithoutChildren(), false, false)

            verify(exactly = 0) { observerErrorCachedResults.onChanged(any()) }
            verify(exactly = 0) { observerErrorNoCacheResults.onChanged(any()) }

            verify {
                observerViewState.onChanged(
                    ListViewState(
                        listOf(
                            storyHeaderItem,
                            mockedCommentViewItem,
                            mockedCommentViewItem
                        ), false
                    )
                )
            }
        }

        @Test
        fun `when headerItem article viewer callback is called then we get the story using cache and post the url to the correct live data`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val articleViewerCallback = slot<() -> Unit>()
            val mockedCommentViewItem = mockk<CommentViewItem>()
            val observer = spyk<Observer<Event<String>>>()
            val urlEmitted = slot<Event<String>>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(any(), any(), any())
            } returns mockedCommentViewItem
            every {
                commentDataMapper.toStoryHeaderViewItem(
                    any(),
                    capture(articleViewerCallback)
                )
            } returns storyHeaderItem
            every { observer.onChanged(capture(urlEmitted)) } just Runs

            commentsListViewModel.navigateToArticle().observeForever(observer)
            commentsListViewModel.init(storyListType, storyType)

            callback.invoke(useCaseResponseWithoutChildren(), false, false)

            articleViewerCallback.invoke()

            coVerify { storiesUseCase.getStory(1, true, TOP) }
            assertEquals("url", urlEmitted.captured.getContentIfNotHandled())
        }

        @Test
        fun `when init is called then we post the title`() {
            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs
            val observer = spyk<Observer<String>>()

            commentsListViewModel.articleTitle().observeForever(observer)
            commentsListViewModel.init(storyListType, storyType)

            coVerify {
                observer.onChanged("title")
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)

            commentsUseCaseCallback.invoke(useCaseResponseWithoutChildren(), false, false)
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)

            commentsUseCaseCallback.invoke(useCaseResponseWithoutChildren(), false, false)
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)

            commentsUseCaseCallback.invoke(useCaseResponseWithChildren(), false, false)

            assertEquals(FULL, commentsPassedToMapper[0].state)
            assertEquals(FULL, commentsPassedToMapper[1].state)
            assertEquals(FULL, commentsPassedToMapper[2].state)
            assertEquals(FULL, commentsPassedToMapper[3].state)

            longClickListenerCallback.invoke(3)

            assertEquals(FULL, commentsPassedToMapper[0].state)
            assertEquals(FULL, commentsPassedToMapper[1].state)
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)

            commentsUseCaseCallback.invoke(useCaseResponseWithChildren(), false, false)

            assertEquals(FULL, commentsPassedToMapper[0].state)
            assertEquals(FULL, commentsPassedToMapper[1].state)
            assertEquals(FULL, commentsPassedToMapper[2].state)
            assertEquals(FULL, commentsPassedToMapper[3].state)

            longClickListenerCallback.invoke(0)

            assertEquals(7, commentsPassedToMapper.size)
            assertEquals(COLLAPSED, commentsPassedToMapper[4].state)
            assertEquals(0, commentsPassedToMapper[4].comment.id)
            assertEquals(FULL, commentsPassedToMapper[5].state)
            assertEquals(2, commentsPassedToMapper[5].comment.id)
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
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    capture(longClickListenerCallback),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)

            commentsUseCaseCallback.invoke(useCaseResponseWithChildren(), false, false)

            assertEquals(FULL, commentsPassedToMapper[0].state)
            assertEquals(FULL, commentsPassedToMapper[1].state)
            assertEquals(FULL, commentsPassedToMapper[2].state)
            assertEquals(FULL, commentsPassedToMapper[3].state)

            longClickListenerCallback.invoke(2)

            assertEquals(7, commentsPassedToMapper.size)
            assertEquals(COLLAPSED, commentsPassedToMapper[6].state)
            assertEquals(2, commentsPassedToMapper[2].comment.id)
        }
    }

    @Nested
    inner class Refresh {

        @Test
        fun `when userManuallyRefreshed is called then retrieve the comments from the use case with use cache as false`() {
            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs

            commentsListViewModel.storyListType = ASK
            commentsListViewModel.userManuallyRefreshed()

            coVerify {
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = false,
                    onResult = any(),
                    requireComments = true,
                    storiesListType = ASK
                )
            }
        }

        @Test
        fun `when automaticallyRefreshed is called then retrieve the comments from the use case with use cache as true`() {
            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs

            commentsListViewModel.storyListType = JOBS
            commentsListViewModel.automaticallyRefreshed()

            coVerify {
                commentsUseCase.retrieveComments(
                    storyId = 1,
                    useCache = true,
                    onResult = any(),
                    requireComments = true,
                    storiesListType = JOBS
                )
            }
        }
    }

    @Nested
    inner class Sort {

        @Test
        fun `when sorting is set to 0 then sort by the servers ordering`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    any(),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)
            commentsListViewModel.updateSortState(0)
            commentsListViewModel.automaticallyRefreshed()

            callback.invoke(useCaseResponseWithManyChildren(), false, false)

            assertEquals(commentsPassedToMapper[0].comment.comment.author, "Jamie")
            assertEquals(commentsPassedToMapper[1].comment.comment.author, "Alex")
            assertEquals(commentsPassedToMapper[2].comment.comment.author, "John")
            assertEquals(commentsPassedToMapper[3].comment.comment.author, "Alice")
            assertEquals(commentsPassedToMapper[4].comment.comment.author, "Audrey")
            assertEquals(commentsPassedToMapper[5].comment.comment.author, "David")
        }

        @Test
        fun `when sorting is set to 1 then sort by the newest parent comments`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    any(),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)
            commentsListViewModel.updateSortState(1)
            commentsListViewModel.automaticallyRefreshed()

            callback.invoke(useCaseResponseWithManyChildren(), false, false)

            assertEquals(commentsPassedToMapper[0].comment.comment.author, "John")
            assertEquals(commentsPassedToMapper[1].comment.comment.author, "Alice")
            assertEquals(commentsPassedToMapper[2].comment.comment.author, "Jamie")
            assertEquals(commentsPassedToMapper[3].comment.comment.author, "Alex")
            assertEquals(commentsPassedToMapper[4].comment.comment.author, "Audrey")
            assertEquals(commentsPassedToMapper[5].comment.comment.author, "David")
        }

        @Test
        fun `when sorting is set to 2 then sort by the oldest parent comments`() {
            val callback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
            val commentsPassedToMapper = mutableListOf<CommentCurrentState>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    capture(callback),
                    any(),
                    any()
                )
            } just Runs
            coEvery {
                commentDataMapper.toCommentViewItem(
                    capture(commentsPassedToMapper),
                    any(),
                    any()
                )
            } returns mockk()

            commentsListViewModel.init(storyListType, storyType)
            commentsListViewModel.updateSortState(2)
            commentsListViewModel.automaticallyRefreshed()

            callback.invoke(useCaseResponseWithManyChildren(), false, false)

            assertEquals(commentsPassedToMapper[0].comment.comment.author, "Audrey")
            assertEquals(commentsPassedToMapper[1].comment.comment.author, "David")
            assertEquals(commentsPassedToMapper[2].comment.comment.author, "Jamie")
            assertEquals(commentsPassedToMapper[3].comment.comment.author, "Alex")
            assertEquals(commentsPassedToMapper[4].comment.comment.author, "John")
            assertEquals(commentsPassedToMapper[5].comment.comment.author, "Alice")
        }
    }

    @Nested
    inner class Share {
        @Test
        fun `when share is called with 0 (share article) then post the correctly formatted title and url`() {
            val observer = spyk<Observer<Event<String>>>()
            val shareText = slot<Event<String>>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs
            every { observer.onChanged(capture(shareText)) } just Runs

            commentsListViewModel.shareUrl().observeForever(observer)
            commentsListViewModel.init(storyListType, storyType)
            commentsListViewModel.share(0)

            assertEquals(
                "title - url",
                shareText.captured.getContentIfNotHandled()
            )
        }

        @Test
        fun `when share is called with 1 (share comments) then post the correctly formatted title and url`() {
            val observer = spyk<Observer<Event<String>>>()
            val shareText = slot<Event<String>>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs
            every { observer.onChanged(capture(shareText)) } just Runs

            commentsListViewModel.shareUrl().observeForever(observer)
            commentsListViewModel.init(storyListType, storyType)
            commentsListViewModel.share(1)

            assertEquals(
                "title - https://news.ycombinator.com/item?id=23",
                shareText.captured.getContentIfNotHandled()
            )
        }

        @Test
        fun `when share is called with 2 (share article and comments) then post the correctly formatted title, article url and comments url`() {
            val observer = spyk<Observer<Event<String>>>()
            val shareText = slot<Event<String>>()

            coEvery {
                commentsUseCase.retrieveComments(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } just Runs
            every { observer.onChanged(capture(shareText)) } just Runs

            commentsListViewModel.shareUrl().observeForever(observer)
            commentsListViewModel.init(storyListType, storyType)
            commentsListViewModel.share(2)

            assertEquals(
                """title
                    |Article - url
                    |Comments - https://news.ycombinator.com/item?id=23""".trimMargin(),
                shareText.captured.getContentIfNotHandled()
            )
        }
    }

    @Test
    fun `when urlClicked callback is called from an item then post the url`() {
        val observer = spyk<Observer<String>>()
        val commentsUseCaseCallback = slot<(List<CommentWithDepth>, Boolean, Boolean) -> Unit>()
        val urlClickedCallback = slot<(String) -> Unit>()

        commentsListViewModel.urlClicked().observeForever(observer)

        coEvery {
            commentsUseCase.retrieveComments(
                any(),
                any(),
                capture(commentsUseCaseCallback),
                any(),
                any()
            )
        } just Runs
        coEvery {
            commentDataMapper.toCommentViewItem(
                any(),
                any(),
                capture(urlClickedCallback)
            )
        } returns mockk()

        commentsListViewModel.init(storyListType, storyType)

        commentsUseCaseCallback.invoke(useCaseResponseWithoutChildren(), false, false)
        urlClickedCallback.invoke("url")

        verify {
            observer.onChanged("url")
        }
    }

    @Test
    fun `when openArticle is called then post the url to the correct live data`() {
        val observer = spyk<Observer<Event<String>>>()
        val urlEmitted = slot<Event<String>>()

        every { observer.onChanged(capture(urlEmitted)) } just Runs
        coEvery { commentsUseCase.retrieveComments(any(), any(), any(), any(), any()) } just Runs

        commentsListViewModel.init(storyListType, storyType)
        commentsListViewModel.navigateToArticle().observeForever(observer)
        commentsListViewModel.openArticle()

        assertEquals("url", urlEmitted.captured.getContentIfNotHandled())
    }

    @Test
    fun `when sortState is updated then post new sorted state`() {
        val observer = spyk<Observer<Int>>()

        commentsListViewModel.sortState().observeForever(observer)
        commentsListViewModel.updateSortState(2)

        verifyOrder {
            observer.onChanged(0)
            observer.onChanged(2)
        }
    }

    private fun useCaseResponseWithoutChildren() = mutableListOf(
        generateComment(
            author = "Jamie",
            commentCount = 0,
            time = "23/08/2020 09:00:00",
            depth = 0
        ),
        generateComment(author = "Alex", commentCount = 0, time = "23/08/2020 10:00:00", depth = 0)
    )

    private fun useCaseResponseWithChildren() = mutableListOf(
        generateComment(
            author = "Jamie",
            commentCount = 1,
            time = "23/08/2020 09:00:00",
            depth = 0
        ),
        generateComment(author = "Alex", commentCount = 0, time = "23/08/2020 10:00:00", depth = 1),
        generateComment(author = "John", commentCount = 1, time = "23/08/2020 11:00:00", depth = 0),
        generateComment(author = "Alice", commentCount = 0, time = "23/08/2020 12:00:00", depth = 1)
    )

    private fun useCaseResponseWithManyChildren() = mutableListOf(
        generateComment(
            author = "Jamie",
            commentCount = 1,
            time = "23/08/2020 09:00:00",
            depth = 0
        ),
        generateComment(author = "Alex", commentCount = 0, time = "23/08/2020 10:00:00", depth = 1),
        generateComment(author = "John", commentCount = 1, time = "23/08/2020 11:00:00", depth = 0),
        generateComment(
            author = "Alice",
            commentCount = 0,
            time = "23/08/2020 12:00:00",
            depth = 1
        ),
        generateComment(
            author = "Audrey",
            commentCount = 1,
            time = "23/08/2020 07:00:00",
            depth = 0
        ),
        generateComment(
            author = "David",
            commentCount = 0,
            time = "23/08/2020 08:00:00",
            depth = 1
        )
    )

    private fun generateComment(
        author: String,
        commentCount: Int,
        time: String,
        depth: Int
    ): CommentWithDepth {
        return CommentWithDepth(
            comment = Comment(
                author = author, commentCount = commentCount, time = DateTime.parse(
                    time,
                    DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
                )
            ),
            depth = depth
        )
    }
}
