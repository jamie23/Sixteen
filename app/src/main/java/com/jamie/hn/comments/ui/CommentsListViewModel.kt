package com.jamie.hn.comments.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.articles.domain.Article
import com.jamie.hn.comments.domain.Comment
import com.jamie.hn.comments.domain.CommentWithDepth
import com.jamie.hn.comments.domain.CommentsUseCase
import kotlinx.coroutines.launch

class CommentsListViewModel(
    private val article: Article,
    private val commentsUseCase: CommentsUseCase,
    private val commentDataMapper: CommentDataMapper
) : ViewModel() {

    private val commentsViewState = MutableLiveData<CommentsViewState>()
    fun commentsViewState(): LiveData<CommentsViewState> = commentsViewState

    fun init() {
        refreshList()
    }

    fun refreshList() {
        viewModelScope.launch {
            commentsViewState.value = CommentsViewState(
                comments = emptyList(),
                refreshing = true
            )

            try {
                val results = commentsUseCase.getComments(article)

                val listAllComments = mutableListOf<CommentWithDepth>()

                results
                    .filter { !it.deleted }
                    .forEach {
                        listAllComments.addAll(it.allCommentsInChain())
                    }

                commentsViewState.postValue(
                    CommentsViewState(
                        comments = commentsToViewItems(listAllComments),
                        refreshing = false
                    )
                )
            } catch (e: Exception) {
                println(e)
                commentsViewState.postValue(
                    CommentsViewState(
                        comments = emptyList(),
                        refreshing = false
                    )
                )
            }
        }
    }

    private fun commentsToViewItems(comments: List<CommentWithDepth>) = comments.map {
            commentDataMapper.toCommentViewItem(
                it
            )
        }

    private fun Comment.allCommentsInChain(depth: Int = 0): List<CommentWithDepth> {
        if (this.listChildComments.isEmpty()) return listOf(CommentWithDepth(this, depth))

        val listComments = mutableListOf<CommentWithDepth>()

        listComments.add(CommentWithDepth(this, depth))

        this.listChildComments
            .filter { !it.deleted }
            .forEach {
                listComments.addAll(it.allCommentsInChain(depth + 1))
            }

        return listComments
    }

    data class CommentsViewState(
        val comments: List<CommentViewItem>,
        val refreshing: Boolean
    )
}
