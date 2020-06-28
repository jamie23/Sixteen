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

    private val articleViewState = MutableLiveData<ArticlesViewState>()
    fun articleViewState(): LiveData<ArticlesViewState> = articleViewState

    private val navigateToComments = MutableLiveData<Event<Long>>()
    fun navigateToComments(): LiveData<Event<Long>> = navigateToComments

    private val navigateToArticle = MutableLiveData<Event<String>>()
    fun navigateToArticle(): LiveData<Event<String>> = navigateToArticle

    fun userManuallyRefreshed() {
        refreshList(false)
    }

    fun automaticallyRefreshed() {
        refreshList(true)
    }

    private fun refreshList(useCachedVersion: Boolean) {
        articleViewState.value = ArticlesViewState(
            stories = emptyList(),
            refreshing = true
        )

        viewModelScope.launch {
            val results = storiesUseCase.getStories(useCachedVersion)
            articleViewState.value = ArticlesViewState(
                stories = results.map {
                    storyDataMapper.toStoryViewItem(
                        it,
                        ::commentsCallback,
                        ::articleViewerCallback
                    )
                },
                refreshing = false
            )
        }
    }

    private fun commentsCallback(id: Long) {
        viewModelScope.launch {
            navigateToComments.value = Event(storiesUseCase.getStory(id).id)
        }
    }

    private fun articleViewerCallback(id: Long) {
        viewModelScope.launch {
            navigateToArticle.value =
                Event(storiesUseCase.getStory(id).url)
        }
    }

    data class ArticlesViewState(
        val stories: List<StoryViewItem>,
        val refreshing: Boolean
    )
}
