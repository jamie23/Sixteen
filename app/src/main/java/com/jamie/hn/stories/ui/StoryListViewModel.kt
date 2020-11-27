package com.jamie.hn.stories.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.core.Event
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.ui.StoryListViewModel.SortChoice.OLDEST
import com.jamie.hn.stories.ui.StoryListViewModel.SortChoice.NEWEST
import com.jamie.hn.stories.ui.StoryListViewModel.SortChoice.STANDARD
import kotlinx.coroutines.launch

class StoryListViewModel(
    private val storyDataMapper: StoryDataMapper,
    private val storiesUseCase: StoriesUseCase
) : ViewModel() {

    private val storyListViewState = MutableLiveData<StoryListViewState>()
    fun storyListViewState(): LiveData<StoryListViewState> = storyListViewState

    private val navigateToComments = MutableLiveData<Event<Int>>()
    fun navigateToComments(): LiveData<Event<Int>> = navigateToComments

    private val navigateToArticle = MutableLiveData<Event<String>>()
    fun navigateToArticle(): LiveData<Event<String>> = navigateToArticle

    private val cachedStoriesNetworkError = MutableLiveData<Event<Unit>>()
    fun cachedStoriesNetworkError(): LiveData<Event<Unit>> = cachedStoriesNetworkError

    private val sortState = MutableLiveData(0)
    fun sortState(): LiveData<Int> = sortState

    fun userManuallyRefreshed() {
        refreshList(false)
    }

    fun automaticallyRefreshed() {
        refreshList(true)
    }

    private fun refreshList(useCachedVersion: Boolean) {
        storyListViewState.value = StoryListViewState(
            stories = emptyList(),
            refreshing = true,
            showNoCachedStoryNetworkError = false
        )

        viewModelScope.launch {
            val results = storiesUseCase.getStories(useCachedVersion)
            storyListViewState.value = StoryListViewState(
                stories = results.stories
                    .sortedWith(
                        // MAYBE WE DONT NEED TO SORT HERE? WE COULD CHECK IF WE NEED WITH A MAP OR SOMETHING?
                        when (getSortEnum(sortState.value ?: -1)) {
                            STANDARD -> sortByServerOrder()
                            NEWEST -> sortByOldestTime().reversed()
                            OLDEST -> sortByOldestTime()
                        })
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

    private fun commentsCallback(id: Int) {
        viewModelScope.launch {
            navigateToComments.value = Event(storiesUseCase.getStory(id, true).story.id)
        }
    }

    private fun articleViewerCallback(id: Int) {
        viewModelScope.launch {
            navigateToArticle.value =
                Event(storiesUseCase.getStory(id, true).story.url)
        }
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
    private fun sortByServerOrder() = compareBy<Story> { it.serverSortedOrder }

    data class StoryListViewState(
        val stories: List<StoryViewItem>,
        val refreshing: Boolean,
        val showNoCachedStoryNetworkError: Boolean
    )

    enum class SortChoice {
        STANDARD, NEWEST, OLDEST
    }
}
