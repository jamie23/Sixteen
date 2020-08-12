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

class CommentsListViewModel(
    private val commentDataMapper: CommentDataMapper,
    private val storyId: Long,
    private val commentsUseCase: CommentsUseCase
) : ViewModel() {

    private val listViewState = MutableLiveData<ListViewState>()
    fun commentsViewState(): LiveData<ListViewState> = listViewState

    private lateinit var commentsViewRepository: CommentsViewRepository

    fun init() {
        commentsViewRepository =
            CommentsViewRepository(
                ::viewStateUpdate
            )
        refreshList()
    }

    fun refreshList() {
        listViewState.value = ListViewState(
            comments = emptyList(),
            refreshing = true
        )

        commentsUseCase.retrieveComments(
            viewModelScope,
            storyId,
            false,
            ::populateUiCommentRepository
        )
    }

    private fun viewStateUpdate(commentList: List<CommentCurrentState>) {
        listViewState.value =
            ListViewState(commentsToViewItems(commentList.filter { it.state != HIDDEN }), false)
    }

    private fun commentsToViewItems(comments: List<CommentCurrentState>) = comments.map {
        commentDataMapper.toCommentViewItem(
            it,
            ::longClickCommentListener
        )
    }

    // Transform list from API to a list with UI state, all items initialised with FULL state shown
    private fun populateUiCommentRepository(listAllComments: List<CommentWithDepth>) {
        commentsViewRepository.commentList = listAllComments.mapIndexed { index, commentWithDepth ->
            CommentCurrentState(comment = commentWithDepth.copy(id = index), state = FULL)
        }
    }

    private fun longClickCommentListener(id: Int) {
        val newStateList = commentsViewRepository.commentList.toMutableList()
        val commentWithState = newStateList[id]
        var childrenNewState: CurrentState

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

    data class ListViewState(
        val comments: List<CommentViewItem>,
        val refreshing: Boolean
    )
}
