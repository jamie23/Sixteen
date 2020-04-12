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

    private val comments = MutableLiveData<List<CommentViewItem>>()
    fun comments(): LiveData<List<CommentViewItem>> = comments

    fun init() {
        viewModelScope.launch {
            val results = commentsUseCase.getComments(article)

            val listAllComments = mutableListOf<CommentWithDepth>()

            results.forEach {
                listAllComments.addAll(it.allCommentsInChain())
            }

            comments.postValue(listAllComments.map { commentDataMapper.toCommentViewItem(it) })
        }
    }

    private fun Comment.allCommentsInChain(depth: Int = 0): List<CommentWithDepth> {
        if (this.listChildComments.isEmpty()) return listOf(CommentWithDepth(this, depth))

        val listComments = mutableListOf<CommentWithDepth>()

        listComments.add(CommentWithDepth(this, depth))

        this.listChildComments.forEach {
            listComments.addAll(it.allCommentsInChain(depth + 1))
        }

        return listComments
    }
}
