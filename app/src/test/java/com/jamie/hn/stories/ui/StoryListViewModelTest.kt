package com.jamie.hn.stories.ui

import androidx.lifecycle.Observer
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.Event
import com.jamie.hn.core.InstantExecutorExtension
import com.jamie.hn.core.StoriesType.ASK
import com.jamie.hn.core.StoriesType.JOBS
import com.jamie.hn.core.StoriesType.NEW
import com.jamie.hn.core.StoriesType.SHOW
import com.jamie.hn.core.StoriesType.TOP
import com.jamie.hn.core.ui.Article
import com.jamie.hn.core.ui.Ask
import com.jamie.hn.core.ui.Jobs
import com.jamie.hn.core.ui.New
import com.jamie.hn.core.ui.Show
import com.jamie.hn.core.ui.Top
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.model.StoryResult
import com.jamie.hn.stories.repository.model.StoriesResult
import com.jamie.hn.stories.ui.StoryListViewModel.StoryListViewState
import com.jamie.hn.stories.ui.StoryListViewModel.StoryTypeStoryId
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(InstantExecutorExtension::class)
class StoryListViewModelTest : BaseTest() {

    @RelaxedMockK
    private lateinit var storyDataMapper: StoryDataMapper

    @MockK
    private lateinit var storiesUseCase: StoriesUseCase

    @MockK
    private lateinit var storyResourceProvider: StoryResourceProvider

    private lateinit var storyListViewModel: StoryListViewModel

    private val story = generateStory(0, "23/08/2020 09:00:00")
    private val olderStory = generateStory(1, "23/08/2020 08:00:00")
    private val newerStory = generateStory(2, "23/08/2020 10:00:00")
    private val storyViewItem = generateStoryViewItem(0)
    private val olderStoryViewItem = generateStoryViewItem(1)
    private val newerStoryViewItem = generateStoryViewItem(2)

    private val storyResults = StoryResult(story)
    private val stories = listOf(story)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { storyResourceProvider.topTitle() } returns "Top stories"
        every { storyResourceProvider.askTitle() } returns "Ask stories"
        every { storyResourceProvider.jobsTitle() } returns "Jobs stories"
        every { storyResourceProvider.newTitle() } returns "New stories"
        every { storyResourceProvider.showTitle() } returns "Show stories"

        coEvery { storiesUseCase.getStories(any(), any()) } returns StoriesResult(stories)
        coEvery { storiesUseCase.getStory(1, true, any()) } returns storyResults
        every { storyDataMapper.toStoryViewItem(story, any(), any()) } returns storyViewItem
        every {
            storyDataMapper.toStoryViewItem(
                olderStory,
                any(),
                any()
            )
        } returns olderStoryViewItem
        every {
            storyDataMapper.toStoryViewItem(
                newerStory,
                any(),
                any()
            )
        } returns newerStoryViewItem

        storyListViewModel =
            StoryListViewModel(storyDataMapper, storiesUseCase, storyResourceProvider)
        storyListViewModel.currentScreen = Top
    }

    @Nested
    inner class Initialise {

        @Test
        fun `when initialise is called with Top then post topTitle for toolbar title`() {
            val observer = spyk<Observer<String>>()

            storyListViewModel.toolbarTitle().observeForever(observer)
            storyListViewModel.initialise(Top)

            verify { observer.onChanged("Top stories") }
        }

        @Test
        fun `when initialise is called with Ask then post askTitle for toolbar title`() {
            val observer = spyk<Observer<String>>()

            storyListViewModel.toolbarTitle().observeForever(observer)
            storyListViewModel.initialise(Ask)

            verify { observer.onChanged("Ask stories") }
        }

        @Test
        fun `when initialise is called with Jobs then post jobsTitle for toolbar title`() {
            val observer = spyk<Observer<String>>()

            storyListViewModel.toolbarTitle().observeForever(observer)
            storyListViewModel.initialise(Jobs)

            verify { observer.onChanged("Jobs stories") }
        }

        @Test
        fun `when initialise is called with New then post newTitle for toolbar title`() {
            val observer = spyk<Observer<String>>()

            storyListViewModel.toolbarTitle().observeForever(observer)
            storyListViewModel.initialise(New)

            verify { observer.onChanged("New stories") }
        }

        @Test
        fun `when initialise is called with Show then post showTitle for toolbar title`() {
            val observer = spyk<Observer<String>>()

            storyListViewModel.toolbarTitle().observeForever(observer)
            storyListViewModel.initialise(Show)

            verify { observer.onChanged("Show stories") }
        }

        @Test
        fun `when initialise is called with an unsupported screen then throw IllegalArgumentException`() {
            val observer = spyk<Observer<String>>()

            storyListViewModel.toolbarTitle().observeForever(observer)
            storyListViewModel.initialise(Show)

            verify { observer.onChanged("Show stories") }
        }

        @Test
        fun `when initialise is called then refresh the story list using the cache using the correct type`() {
            storyListViewModel.initialise(Top)
            coVerify { storiesUseCase.getStories(true, TOP) }
        }

        @Test
        fun `when initialise is called with an unsupported screen then thrown an IllegalArgumentException and do not refresh list`() {
            val exception = assertThrows<IllegalArgumentException> {
                storyListViewModel.initialise(Article("url"))
            }

            assertEquals("Unsupported title", exception.message)
            coVerify(exactly = 0) { storiesUseCase.getStories(true, TOP) }
        }
    }


    @Nested
    inner class RefreshList {

        @Nested
        inner class ManualRefresh {

            @Test
            fun `when refresh is called then we first emit empty list and is refreshing`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.userManuallyRefreshed()

                verify {
                    observer.onChanged(
                        StoryListViewState(
                            stories = emptyList(),
                            refreshing = true,
                            showNoCachedStoryNetworkError = false
                        )
                    )
                }
            }

            @Test
            fun `when refresh is called then we call the usecase with false, the mapping, map the results with the mapper and emit the view state`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.userManuallyRefreshed()

                coVerify { storiesUseCase.getStories(false, TOP) }
                verify { storyDataMapper.toStoryViewItem(story, any(), any()) }
                verify {
                    observer.onChanged(
                        StoryListViewState(
                            stories = listOf(storyViewItem),
                            refreshing = false,
                            showNoCachedStoryNetworkError = false
                        )
                    )
                }
            }

            @Test
            fun `when refresh is called and currentScreen is Top then the usecase should be called with storiesType TOP`() {
                storyListViewModel.currentScreen = Top
                storyListViewModel.userManuallyRefreshed()

                coVerify { storiesUseCase.getStories(false, TOP) }
            }

            @Test
            fun `when refresh is called and currentScreen is Ask then the usecase should be called with storiesType ASK`() {
                storyListViewModel.currentScreen = Ask
                storyListViewModel.userManuallyRefreshed()

                coVerify { storiesUseCase.getStories(false, ASK) }
            }

            @Test
            fun `when refresh is called and currentScreen is Jobs then the usecase should be called with storiesType JOBS`() {
                storyListViewModel.currentScreen = Jobs
                storyListViewModel.userManuallyRefreshed()

                coVerify { storiesUseCase.getStories(false, JOBS) }
            }

            @Test
            fun `when refresh is called and currentScreen is New then the usecase should be called with storiesType NEW`() {
                storyListViewModel.currentScreen = New
                storyListViewModel.userManuallyRefreshed()

                coVerify { storiesUseCase.getStories(false, NEW) }
            }

            @Test
            fun `when refresh is called and currentScreen is Show then the usecase should be called with storiesType SHOW`() {
                storyListViewModel.currentScreen = Show
                storyListViewModel.userManuallyRefreshed()

                coVerify { storiesUseCase.getStories(false, SHOW) }
            }

            @Test
            fun `when refresh is called but there is a network failure and returns cached stories then emit event for network failure with cached results`() {
                val observerStories = spyk<Observer<StoryListViewState>>()
                val observerNetworkError = spyk<Observer<Event<Unit>>>()

                coEvery { storiesUseCase.getStories(any(), any()) } returns StoriesResult(
                    stories,
                    true
                )

                storyListViewModel.storyListViewState().observeForever(observerStories)
                storyListViewModel.cachedStoriesNetworkError().observeForever(observerNetworkError)
                storyListViewModel.userManuallyRefreshed()

                verify { storyDataMapper.toStoryViewItem(story, any(), any()) }
                verify {
                    observerStories.onChanged(
                        StoryListViewState(
                            stories = listOf(storyViewItem),
                            refreshing = false,
                            showNoCachedStoryNetworkError = false
                        )
                    )
                }
                verify { observerNetworkError.onChanged(any()) }
            }

            @Test
            fun `when refresh is called but there is a network failure and returns no stories then do not emit event for network failure with cached results but set NoCachedError to true`() {
                val observerStories = spyk<Observer<StoryListViewState>>()
                val observerNetworkError = spyk<Observer<Event<Unit>>>()

                coEvery { storiesUseCase.getStories(any(), any()) } returns StoriesResult(
                    emptyList(),
                    true
                )

                storyListViewModel.storyListViewState().observeForever(observerStories)
                storyListViewModel.cachedStoriesNetworkError().observeForever(observerNetworkError)
                storyListViewModel.userManuallyRefreshed()

                verify {
                    observerStories.onChanged(
                        StoryListViewState(
                            stories = emptyList(),
                            refreshing = false,
                            showNoCachedStoryNetworkError = true
                        )
                    )
                }
                verify(exactly = 0) { storyDataMapper.toStoryViewItem(any(), any(), any()) }
                verify(exactly = 0) { observerNetworkError.onChanged(any()) }
            }
        }

        @Nested
        inner class AutomaticRefresh {

            @Test
            fun `when refresh is called then we first emit empty list and is refreshing`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.automaticallyRefreshed()

                verify {
                    observer.onChanged(
                        StoryListViewState(
                            stories = emptyList(),
                            refreshing = true,
                            showNoCachedStoryNetworkError = false
                        )
                    )
                }
            }

            @Test
            fun `when refresh is called then we call the usecase with true, map the results with the mapper and emit the view state`() {
                val observer = spyk<Observer<StoryListViewState>>()

                storyListViewModel.storyListViewState().observeForever(observer)
                storyListViewModel.automaticallyRefreshed()

                coVerify { storiesUseCase.getStories(true, TOP) }
                verify { storyDataMapper.toStoryViewItem(story, any(), any()) }
                verify {
                    observer.onChanged(
                        StoryListViewState(
                            stories = listOf(storyViewItem),
                            refreshing = false,
                            showNoCachedStoryNetworkError = false
                        )
                    )
                }
            }
        }
    }

    @Nested
    inner class Sorting {

        @Test
        fun `when sorting is set to 0 then sort by the servers ordering`() {
            val observer = spyk<Observer<StoryListViewState>>()
            coEvery { storiesUseCase.getStories(any(), any()) } returns StoriesResult(
                listOf(
                    story,
                    olderStory,
                    newerStory
                )
            )

            storyListViewModel.storyListViewState().observeForever(observer)
            storyListViewModel.updateSortState(0)
            storyListViewModel.userManuallyRefreshed()

            verifyOrder {
                observer.onChanged(
                    StoryListViewState(
                        stories = emptyList(),
                        refreshing = true,
                        showNoCachedStoryNetworkError = false
                    )
                )
                storyDataMapper.toStoryViewItem(story, any(), any())
                storyDataMapper.toStoryViewItem(olderStory, any(), any())
                storyDataMapper.toStoryViewItem(newerStory, any(), any())
                observer.onChanged(
                    StoryListViewState(
                        stories = listOf(storyViewItem, olderStoryViewItem, newerStoryViewItem),
                        refreshing = false,
                        showNoCachedStoryNetworkError = false
                    )
                )
            }
        }

        @Test
        fun `when sorting is set to 1 then sort by newest stories`() {
            val observer = spyk<Observer<StoryListViewState>>()
            coEvery { storiesUseCase.getStories(any(), any()) } returns StoriesResult(
                listOf(
                    story,
                    olderStory,
                    newerStory
                )
            )

            storyListViewModel.storyListViewState().observeForever(observer)
            storyListViewModel.updateSortState(1)
            storyListViewModel.userManuallyRefreshed()

            verifyOrder {
                observer.onChanged(
                    StoryListViewState(
                        stories = emptyList(),
                        refreshing = true,
                        showNoCachedStoryNetworkError = false
                    )
                )
                storyDataMapper.toStoryViewItem(newerStory, any(), any())
                storyDataMapper.toStoryViewItem(story, any(), any())
                storyDataMapper.toStoryViewItem(olderStory, any(), any())
                observer.onChanged(
                    StoryListViewState(
                        stories = listOf(newerStoryViewItem, storyViewItem, olderStoryViewItem),
                        refreshing = false,
                        showNoCachedStoryNetworkError = false
                    )
                )
            }
        }

        @Test
        fun `when sorting is set to 2 then sort by the oldest stories`() {
            val observer = spyk<Observer<StoryListViewState>>()
            coEvery { storiesUseCase.getStories(any(), any()) } returns StoriesResult(
                listOf(
                    story,
                    olderStory,
                    newerStory
                )
            )

            storyListViewModel.storyListViewState().observeForever(observer)
            storyListViewModel.updateSortState(2)
            storyListViewModel.userManuallyRefreshed()

            verifyOrder {
                observer.onChanged(
                    StoryListViewState(
                        stories = emptyList(),
                        refreshing = true,
                        showNoCachedStoryNetworkError = false
                    )
                )
                storyDataMapper.toStoryViewItem(olderStory, any(), any())
                storyDataMapper.toStoryViewItem(story, any(), any())
                storyDataMapper.toStoryViewItem(newerStory, any(), any())
                observer.onChanged(
                    StoryListViewState(
                        stories = listOf(olderStoryViewItem, storyViewItem, newerStoryViewItem),
                        refreshing = false,
                        showNoCachedStoryNetworkError = false
                    )
                )
            }
        }
    }


    @Test
    fun `when comments callback is called then we get the story using cache version and post the id  and the correct storyType to correct live data`() {
        val observer = spyk<Observer<Event<StoryTypeStoryId>>>()
        val commentsCallback = slot<(id: Int) -> Unit>()
        val storyTypeStoryId = slot<Event<StoryTypeStoryId>>()

        every {
            storyDataMapper.toStoryViewItem(
                any(),
                capture(commentsCallback),
                any()
            )
        } returns storyViewItem
        every { observer.onChanged(capture(storyTypeStoryId)) } just runs

        storyListViewModel.navigateToComments().observeForever(observer)
        storyListViewModel.automaticallyRefreshed()

        commentsCallback.captured.invoke(1)

        val resultStoryTypeStoryId = storyTypeStoryId.captured.getContentIfNotHandled()!!
        coVerify { storiesUseCase.getStory(1, true, TOP) }
        assertEquals(0, resultStoryTypeStoryId.storyId)
        assertEquals(TOP, resultStoryTypeStoryId.storyType)
    }

    @Test
    fun `when article viewer callback is called then we get the story using cache and post the url to the correct live data`() {
        val observer = spyk<Observer<Event<String>>>()
        val articleViewerCallback = slot<(id: Int) -> Unit>()
        val urlEmitted = slot<Event<String>>()

        every {
            storyDataMapper.toStoryViewItem(
                any(),
                any(),
                capture(articleViewerCallback)
            )
        } returns storyViewItem
        every { observer.onChanged(capture(urlEmitted)) } just runs

        storyListViewModel.navigateToArticle().observeForever(observer)
        storyListViewModel.automaticallyRefreshed()

        articleViewerCallback.captured.invoke(1)

        coVerify { storiesUseCase.getStory(1, true, TOP) }
        assertEquals("url", urlEmitted.captured.getContentIfNotHandled())
    }

    @Test
    fun `when sortState is updated then post new sorted state`() {
        val observer = spyk<Observer<Int>>()

        storyListViewModel.sortState().observeForever(observer)
        storyListViewModel.updateSortState(2)

        verify {
            observer.onChanged(0)
            observer.onChanged(2)
        }
    }

    private fun generateStory(id: Int, time: String) = Story(
        id = id,
        time = DateTime.parse(
            time,
            DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        ),
        url = "url"
    )

    private fun generateStoryViewItem(id: Int) =
        StoryViewItem(
            id = id,
            author = "Jamie",
            comments = "1",
            score = "2",
            scoreText = "points",
            time = "3",
            title = "title",
            url = "url",
            commentsCallback = { },
            storyViewerCallback = { }
        )
}
