package com.jamie.hn.comments.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
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
        commentsViewRepository = CommentsViewRepository(
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

            commentsViewRepository.fullCommentList = listAllComments
        }
    }

    private fun commentsToViewItems(comments: List<CommentWithDepth>) = comments.map {
        commentDataMapper.toCommentViewItem(
            it,
            ::collapseCallback
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

    private fun collapseCallback(position: Int) {
        println("Jamie $position")
    }

    private fun viewStateUpdate(commentList: List<CommentWithDepth>) {
        commentsViewState.value =
            CommentsViewState(commentsToViewItems(commentList), false)
    }

    data class CommentsViewState(
        val comments: List<CommentViewItem>,
        val refreshing: Boolean
    )
}
