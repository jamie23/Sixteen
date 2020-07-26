package com.jamie.hn.comments.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.repository.CommentsViewRepository
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.COLLAPSED
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.comments.ui.repository.model.CurrentState.HIDDEN
import com.jamie.hn.stories.repository.StoriesRepository
import kotlinx.coroutines.launch

class CommentsListViewModel(
    private val commentDataMapper: CommentDataMapper,
    private val repository: StoriesRepository,
    private val storyId: Long
) : ViewModel() {

    private val commentsViewState = MutableLiveData<CommentsViewState>()
    fun commentsViewState(): LiveData<CommentsViewState> = commentsViewState

    private lateinit var commentsViewRepository: CommentsViewRepository

    fun init() {
        commentsViewRepository =
            CommentsViewRepository(
                ::viewStateUpdate
            )
        refreshList()
    }

    fun refreshList() {
        commentsViewState.value = CommentsViewState(
            comments = emptyList(),
            refreshing = true
        )

        viewModelScope.launch {
            val story = repository.story(storyId, false)

            val listAllComments = mutableListOf<CommentWithDepth>()

            story.comments.forEach {
                listAllComments.addAll(it.allCommentsInChain())
            }

            updateListToCurrentStateItems(listAllComments)
        }
    }

    // Transform list from API into currentStateList, all items initialised with FULL state shown
    private fun updateListToCurrentStateItems(listAllComments: MutableList<CommentWithDepth>) {
        commentsViewRepository.commentList = listAllComments.mapIndexed { index, commentWithDepth ->
            CommentCurrentState(comment = commentWithDepth.copy(id = index), state = FULL)
        }
    }

    private fun commentsToViewItems(comments: List<CommentCurrentState>) = comments.map {
        commentDataMapper.toCommentViewItem(
            it,
            ::longClickCommentListener
        )
    }

    private fun Comment.allCommentsInChain(depth: Int = 0): List<CommentWithDepth> {
        if (this.comments.isEmpty()) return listOf(
            CommentWithDepth(
                this,
                depth
            )
        )

        val listComments = mutableListOf<CommentWithDepth>()

        listComments.add(
            CommentWithDepth(
                this,
                depth
            )
        )

        this.comments.forEach {
            listComments.addAll(it.allCommentsInChain(depth + 1))
        }

        return listComments
    }

    private fun longClickCommentListener(id: Int) {
        val newState = commentsViewRepository.commentList.toMutableList()
        val comment = newState[id]
        val depth = comment.comment.depth

        // If it is not a collapsed thread, collapse the thread
        if (comment.state == FULL) {
            comment.state = COLLAPSED

            if (comment.comment.id != newState.lastIndex) {
                val idOfNextSibling = newState.subList(id + 1, newState.size)
                    .firstOrNull { it.comment.depth <= depth }?.comment?.id ?: newState.size

                for (i in id + 1 until idOfNextSibling) {
                    newState[i].state = HIDDEN
                }
            }

            commentsViewRepository.commentList = newState
            return
        }

        // If it is a collapsed thread, uncollapse it
        comment.state = FULL

        if (comment.comment.id != newState.lastIndex) {
            val idOfNextSibling = newState.subList(id + 1, newState.size)
                .firstOrNull { it.comment.depth <= depth }?.comment?.id ?: newState.size

            for (i in id + 1 until idOfNextSibling) {
                newState[i].state = FULL
            }
        }

        commentsViewRepository.commentList = newState
    }

    private fun viewStateUpdate(commentList: List<CommentCurrentState>) {
        val commentsToSend = commentList.filter { it.state != HIDDEN }

        commentsViewState.value =
            CommentsViewState(commentsToViewItems(commentsToSend), false)
    }

    data class CommentsViewState(
        val comments: List<CommentViewItem>,
        val refreshing: Boolean
    )
}
