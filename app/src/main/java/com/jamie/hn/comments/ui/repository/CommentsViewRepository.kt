package com.jamie.hn.comments.ui.repository

import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import kotlin.properties.Delegates

// Easy way to store the full list of items, with their collapsed/hidden state
class CommentsViewRepository(
    private val listUpdated: (List<CommentCurrentState>) -> Unit
) {

    var commentList: List<CommentCurrentState> by Delegates.observable(emptyList()) { _, _, newViewState ->
        listUpdated.invoke(newViewState)
    }
}
