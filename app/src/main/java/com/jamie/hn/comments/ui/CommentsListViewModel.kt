package com.jamie.hn.comments.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.comments.domain.CommentsUseCase
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.repository.CommentsViewRepository
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.COLLAPSED
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.comments.ui.repository.model.CurrentState.HIDDEN
import com.jamie.hn.core.Event
import com.jamie.hn.stories.domain.StoriesUseCase
import kotlinx.coroutines.launch

class CommentsListViewModel(
    private val commentDataMapper: CommentDataMapper,
    private val storyId: Int,
    private val commentsUseCase: CommentsUseCase,
    private val storiesUseCase: StoriesUseCase
) : ViewModel() {

    private val listViewState = MutableLiveData<ListViewState>()
    fun commentsViewState(): LiveData<ListViewState> = listViewState

    private val networkErrorCachedResults = MutableLiveData<Event<Unit>>()
    fun networkErrorCachedResults(): LiveData<Event<Unit>> = networkErrorCachedResults

    private val networkErrorNoCacheResults = MutableLiveData<Event<Unit>>()
    fun networkErrorNoCacheResults(): LiveData<Event<Unit>> = networkErrorNoCacheResults

    private val urlClicked = MutableLiveData<String>()
    fun urlClicked(): LiveData<String> = urlClicked

    private val navigateToArticle = MutableLiveData<Event<String>>()
    fun navigateToArticle(): LiveData<Event<String>> = navigateToArticle

    private lateinit var commentsViewRepository: CommentsViewRepository

    fun userManuallyRefreshed() {
        refreshList(false)
    }

    private fun automaticallyRefreshed() {
        refreshList(true)
    }

    fun init() {
        commentsViewRepository =
            CommentsViewRepository(
                ::viewStateUpdate
            )

        automaticallyRefreshed()
    }

    private fun refreshList(useCachedVersion: Boolean) {
        listViewState.value = ListViewState(
            comments = emptyList(),
            refreshing = true
        )

        viewModelScope.launch {
            commentsUseCase.retrieveComments(
                storyId = storyId,
                useCache = useCachedVersion,
                onResult = ::populateUiCommentRepository,
                requireComments = true
            )
        }
    }

    private fun viewStateUpdate(commentList: List<CommentCurrentState>) {
        viewModelScope.launch {
            listViewState.value =
                ListViewState(commentsToViewItems(commentList.filter { it.state != HIDDEN }), false)
        }
    }

    private suspend fun commentsToViewItems(comments: List<CommentCurrentState>): List<ViewItem> {
        val viewStateComments = comments.map {
            commentDataMapper.toCommentViewItem(
                it,
                ::longClickCommentListener,
                ::urlClicked
            )
        }

        return addHeader(viewStateComments)
    }

    fun openArticle() {
        viewModelScope.launch {
            navigateToArticle.value =
                Event(storiesUseCase.getStory(storyId, true).story.url)
        }
    }

    // Transform list from API to a list with UI state, all items initialised with FULL state shown
    private fun populateUiCommentRepository(
        listAllComments: List<CommentWithDepth>,
        networkFailure: Boolean,
        useCachedVersion: Boolean
    ) {

        if (listAllComments.isEmpty() && networkFailure) {
            networkErrorNoCacheResults.value = Event(Unit)
            return
        }

        if (listAllComments.isNotEmpty() && networkFailure && !useCachedVersion) {
            networkErrorCachedResults.value = Event(Unit)
        }

        commentsViewRepository.commentList = listAllComments.mapIndexed { index, commentWithDepth ->
            CommentCurrentState(comment = commentWithDepth.copy(id = index), state = FULL)
        }
    }

    private suspend fun addHeader(listAllComments: List<ViewItem>): List<ViewItem> {
        val result = storiesUseCase.getStory(storyId, true).story

        val headerItem = commentDataMapper.toStoryHeaderViewItem(
            story = result,
            storyViewerCallback = ::articleViewerCallback
        )

        // Place the header item at the start of a new list followed by comments
        return listOf(headerItem) + listAllComments
    }

    private fun articleViewerCallback() {
        openArticle()
    }

    private fun longClickCommentListener(id: Int) {
        val newStateList = commentsViewRepository.commentList.toMutableList()
        val commentWithState = newStateList[id]
        val childrenNewState: CurrentState

        if (commentWithState.state == FULL) {
            commentWithState.state = COLLAPSED
            childrenNewState = HIDDEN
        } else {
            commentWithState.state = FULL
            childrenNewState = FULL
        }

        updateVisibilityStateOfItem(
            childrenNewState,
            commentWithState,
            newStateList,
            commentWithState.comment.depth,
            id
        )

        commentsViewRepository.commentList = newStateList
    }

    private fun updateVisibilityStateOfItem(
        newState: CurrentState,
        commentWithState: CommentCurrentState,
        newStateList: MutableList<CommentCurrentState>,
        depth: Int,
        id: Int
    ) {
        if (commentWithState.comment.id != newStateList.lastIndex) {
            val idOfNextSibling = newStateList.subList(id + 1, newStateList.size)
                .firstOrNull { it.comment.depth <= depth }?.comment?.id ?: newStateList.size

            for (i in id + 1 until idOfNextSibling) {
                newStateList[i].state = newState
            }
        }
    }

    private fun urlClicked(url: String) {
        urlClicked.value = url
    }

    data class ListViewState(
        val comments: List<ViewItem>,
        val refreshing: Boolean
    )
}
