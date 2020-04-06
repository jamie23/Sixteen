package com.jamie.hn.comments.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.articles.domain.Article
import com.jamie.hn.comments.CommentsUseCase
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

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
            comments.postValue(results.map { commentDataMapper.toCommentViewItem(it) })
        }
    }
}
