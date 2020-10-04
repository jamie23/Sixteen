package com.jamie.hn.comments.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.ui.extensions.fixUrlSpans
import com.jamie.hn.comments.ui.extensions.italiciseQuotes
import com.jamie.hn.comments.ui.extensions.removeAppendedNewLines
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.core.ui.CoreDataMapper

class CommentDataMapper(
    private val commentsResourceProvider: CommentsResourceProvider,
    private val coreDataMapper: CoreDataMapper
) {

    fun toCommentViewItem(
        wrapper: CommentCurrentState,
        collapseCallback: (Int) -> Unit,
        urlClickedCallback: (String) -> Unit
    ): CommentViewItem {
        val comment = wrapper.comment.comment
        val depth = wrapper.comment.depth

        return CommentViewItem(
            author = comment.author,
            text = processText(comment.text, urlClickedCallback),
            time = coreDataMapper.time(comment.time),
            depth = depth,
            showTopDivider = depth == 0,
            authorAndHiddenChildren = authorAndHiddenChildren(comment),
            longClickCommentListener = collapseCallback,
            id = wrapper.comment.id,
            state = wrapper.state
        )
    }

    fun processText(text: String, urlClickedCallback: (String) -> Unit) =
        text.fixUrlSpans(urlClickedCallback)
            .italiciseQuotes()
            .removeAppendedNewLines()

    private fun authorAndHiddenChildren(comment: Comment) =
        "${comment.author} [${comment.commentCount} ${commentsResourceProvider.children()}]"
}
