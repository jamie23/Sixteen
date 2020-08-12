package com.jamie.hn.comments.ui

import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.core.ui.CoreDataMapper

class CommentDataMapper(
    private val commentsResourceProvider: CommentsResourceProvider,
    private val coreDataMapper: CoreDataMapper
) {

    fun toCommentViewItem(
        wrapper: CommentCurrentState,
        collapseCallback: (Int) -> Unit
    ): CommentViewItem {
        val comment = wrapper.comment.comment
        val depth = wrapper.comment.depth

        return CommentViewItem(
            author = comment.author,
            text = htmlTextParser(comment.text).removeAppendedNewLines(),
            time = coreDataMapper.time(comment.time),
            depth = depth,
            showTopDivider = depth == 0,
            authorAndHiddenChildren = authorAndHiddenChildren(comment),
            longClickCommentListener = collapseCallback,
            id = wrapper.comment.id,
            state = wrapper.state
        )
    }

    private fun String.removeAppendedNewLines() =
        if (this.isNotEmpty() && this[this.length - 1] == '\n') {
            this.subSequence(0, this.length - 2).toString()
        } else {
            this
        }

    fun htmlTextParser(text: String) =
        HtmlCompat.fromHtml(text, FROM_HTML_MODE_LEGACY).toString()

    private fun authorAndHiddenChildren(comment: Comment) =
        "${comment.author} [${comment.commentCount} ${commentsResourceProvider.children()}]"
}
