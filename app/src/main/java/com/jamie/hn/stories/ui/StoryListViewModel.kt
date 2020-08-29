package com.jamie.hn.stories.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.core.Event
import kotlinx.coroutines.launch

class StoryListViewModel(
    private val storyDataMapper: StoryDataMapper,
    private val storiesUseCase: StoriesUseCase
) : ViewModel() {

    private val storyListViewState = MutableLiveData<StoryListViewState>()
    fun storyListViewState(): LiveData<StoryListViewState> = storyListViewState

    private val navigateToComments = MutableLiveData<Event<Long>>()
    fun navigateToComments(): LiveData<Event<Long>> = navigateToComments

    private val navigateToArticle = MutableLiveData<Event<String>>()
    fun navigateToArticle(): LiveData<Event<String>> = navigateToArticle

    private val cachedStoriesNetworkError = MutableLiveData<Event<Unit>>()
    fun cachedStoriesNetworkError(): LiveData<Event<Unit>> = cachedStoriesNetworkError

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
                stories = results.stories.map {
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

    private fun commentsCallback(id: Long) {
        viewModelScope.launch {
            navigateToComments.value = Event(storiesUseCase.getStory(id, true).story.id)
        }
    }

    private fun articleViewerCallback(id: Long) {
        viewModelScope.launch {
            navigateToArticle.value =
                Event(storiesUseCase.getStory(id, true).story.url)
        }
    }

    data class StoryListViewState(
        val stories: List<StoryViewItem>,
        val refreshing: Boolean,
        val showNoCachedStoryNetworkError: Boolean
    )
}
