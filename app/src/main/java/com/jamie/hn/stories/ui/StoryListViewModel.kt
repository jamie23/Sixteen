package com.jamie.hn.stories.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.core.Event
import com.jamie.hn.core.StoriesListType
import com.jamie.hn.core.StoryType
import com.jamie.hn.core.ui.Ask
import com.jamie.hn.core.ui.Jobs
import com.jamie.hn.core.ui.New
import com.jamie.hn.core.ui.Screen
import com.jamie.hn.core.ui.Show
import com.jamie.hn.core.ui.Top
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.ui.StoryListViewModel.SortChoice.OLDEST
import com.jamie.hn.stories.ui.StoryListViewModel.SortChoice.NEWEST
import com.jamie.hn.stories.ui.StoryListViewModel.SortChoice.STANDARD
import kotlinx.coroutines.launch

class StoryListViewModel(
    private val storyDataMapper: StoryDataMapper,
    private val storiesUseCase: StoriesUseCase,
    private val storyResourceProvider: StoryResourceProvider
) : ViewModel() {

    private val storyListViewState = MutableLiveData<StoryListViewState>()
    fun storyListViewState(): LiveData<StoryListViewState> = storyListViewState

    private val navigateToComments = MutableLiveData<Event<StoryData>>()
    fun navigateToComments(): LiveData<Event<StoryData>> = navigateToComments

    private val navigateToArticle = MutableLiveData<Event<String>>()
    fun navigateToArticle(): LiveData<Event<String>> = navigateToArticle

    private val cachedStoriesNetworkError = MutableLiveData<Event<Unit>>()
    fun cachedStoriesNetworkError(): LiveData<Event<Unit>> = cachedStoriesNetworkError

    private val sortState = MutableLiveData(0)
    fun sortState(): LiveData<Int> = sortState

    private val toolbarTitle = MutableLiveData<String>()
    fun toolbarTitle(): LiveData<String> = toolbarTitle

    lateinit var currentScreen: Screen

    fun userManuallyRefreshed() {
        refreshList(false)
    }

    fun automaticallyRefreshed() {
        refreshList(true)
    }

    fun initialise(currentScreen: Screen) {
        toolbarTitle.value = getTitle(currentScreen)
        this.currentScreen = currentScreen
        automaticallyRefreshed()
    }

    private fun getTitle(currentScreen: Screen) =
        when (currentScreen) {
            Top -> storyResourceProvider.topTitle()
            Ask -> storyResourceProvider.askTitle()
            Jobs -> storyResourceProvider.jobsTitle()
            New -> storyResourceProvider.newTitle()
            Show -> storyResourceProvider.showTitle()
            else -> throw IllegalArgumentException("Unsupported title")
        }

    private fun refreshList(useCachedVersion: Boolean) {
        storyListViewState.value = StoryListViewState(
            stories = emptyList(),
            refreshing = true,
            showNoCachedStoryNetworkError = false
        )

        viewModelScope.launch {
            val results =
                storiesUseCase.getStories(useCachedVersion, getStoryTypeFromScreen(currentScreen))
            val sortedList = if (getSortEnum(sortState.value ?: -1) == STANDARD) {
                results.stories
            } else {
                sortList(results.stories)
            }

            storyListViewState.value = StoryListViewState(
                stories = sortedList
                    .map {
                        storyDataMapper.toStoryViewItem(
                            it,
                            ::commentsCallback,
                            ::articleViewerCallback
                        )
                    },
                refreshing = false,
                showNoCachedStoryNetworkError = results.stories.isEmpty() && results.networkFailure
            )

            if (results.stories.isNotEmpty() && results.networkFailure && !useCachedVersion) {
                cachedStoriesNetworkError.value = Event(Unit)
            }
        }
    }

    private fun sortList(listStories: List<Story>) = listStories.sortedWith(
        when (getSortEnum(sortState.value ?: -1)) {
            NEWEST -> sortByOldestTime().reversed()
            OLDEST -> sortByOldestTime()
            else -> throw IllegalArgumentException("Erroneous sort option chosen")
        }
    )

    private fun commentsCallback(id: Int) {
        val storiesListType = getStoryTypeFromScreen(currentScreen)

        viewModelScope.launch {
            val story = storiesUseCase.getStory(
                id = id,
                useCachedVersion = true,
                storiesListType = storiesListType,
                requireText = false
            ).story

            navigateToComments.value = Event(
                StoryData(
                    storyId = story.id,
                    storiesListType = storiesListType,
                    storyType = getStoryType(story.title)
                )
            )
        }
    }

    private fun articleViewerCallback(id: Int) {
        viewModelScope.launch {
            navigateToArticle.value =
                Event(
                    storiesUseCase.getStory(
                        id = id,
                        useCachedVersion = true,
                        storiesListType = getStoryTypeFromScreen(currentScreen),
                        requireText = false
                    ).story.url
                )
        }
    }

    private fun getStoryType(title: String): StoryType {
        if (title.startsWith("Ask HN:")) {
            return StoryType.ASK
        }

        return StoryType.STANDARD
    }

    fun updateSortState(which: Int) {
        sortState.value = which
    }

    private fun getSortEnum(order: Int) =
        when (order) {
            0 -> STANDARD
            1 -> NEWEST
            2 -> OLDEST
            else -> throw IllegalArgumentException("Erroneous sort option chosen")
        }

    private fun sortByOldestTime() = compareBy<Story> { it.time }

    private fun getStoryTypeFromScreen(screen: Screen) =
        when (screen) {
            Top -> StoriesListType.TOP
            Ask -> StoriesListType.ASK
            Jobs -> StoriesListType.JOBS
            New -> StoriesListType.NEW
            Show -> StoriesListType.SHOW
            else -> throw IllegalArgumentException("Unsupported type of screen for fetching stories")
        }

    data class StoryListViewState(
        val stories: List<StoryViewItem>,
        val refreshing: Boolean,
        val showNoCachedStoryNetworkError: Boolean
    )

    data class StoryData(
        val storyId: Int,
        val storiesListType: StoriesListType,
        val storyType: StoryType
    )

    enum class SortChoice {
        STANDARD, NEWEST, OLDEST
    }
}
