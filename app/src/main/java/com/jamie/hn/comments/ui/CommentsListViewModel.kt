package com.jamie.hn.comments.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.articles.domain.Article
import com.jamie.hn.comments.CommentsUseCase
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class CommentsListViewModel(
    private val commentsUseCase: CommentsUseCase,
    private val article: Article
) : ViewModel() {

    fun init() {
        viewModelScope.launch {
            val comments = commentsUseCase.getComments(article)
            println("Jamie $comments")
        }
    }
}
