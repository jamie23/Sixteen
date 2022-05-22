package com.jamie.hn.comments.ui

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.ui.extensions.fixUrlSpans
import com.jamie.hn.comments.ui.extensions.italiciseQuotes
import com.jamie.hn.comments.ui.extensions.removeAppendedNewLines
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.HEADER
import com.jamie.hn.core.StoryType
import com.jamie.hn.core.ui.CoreDataMapper
import com.jamie.hn.stories.domain.model.Story

class CommentDataMapper(
    private val resourceProvider: CommentsResourceProvider,
    private val coreDataMapper: CoreDataMapper
) {

    fun toCommentViewItem(
        wrapper: CommentCurrentState,
        storyAuthor: String,
        collapseCallback: (Int) -> Unit,
        urlClickedCallback: (String) -> Unit
    ): CommentViewItem {
        val comment = wrapper.comment.comment
        val depth = wrapper.comment.depth

        return CommentViewItem(
            id = wrapper.comment.id,
            state = wrapper.state,
            author = comment.author,
            isOP = storyAuthor == wrapper.comment.comment.author,
            time = coreDataMapper.time(comment.time),
            text = processText(comment.text, urlClickedCallback),
            depth = depth,
            authorAndHiddenChildren = authorAndHiddenChildren(
                comment,
                storyAuthor == wrapper.comment.comment.author
            ),
            showTopDivider = depth == 0,
            clickCommentListener = collapseCallback
        )
    }

    fun toStoryHeaderViewItem(
        story: Story,
        urlClickedCallback: (String) -> Unit,
        storyViewerCallback: () -> Unit
    ) = HeaderViewItem(
        id = story.id,
        state = HEADER,
        author = story.author,
        time = coreDataMapper.time(story.time),
        comments = comments(story.commentCount),
        score = story.score.toString(),
        scoreText = scoreText(story.score),
        title = story.title,
        url = story.domain,
        text = processText(story.text, urlClickedCallback),
        showAskText = story.type == StoryType.ASK,
        showNavigateToArticle = story.type != StoryType.ASK,
        storyViewerCallback = storyViewerCallback
    )

    fun processText(text: String, urlClickedCallback: (String) -> Unit) =
        text.fixUrlSpans(urlClickedCallback)
            .italiciseQuotes()
            .removeAppendedNewLines()

    private fun authorAndHiddenChildren(comment: Comment, isOP: Boolean) = if (isOP) {
        authorAndHiddenChildrenOP(comment)
    } else {
        authorAndHiddenChildrenNotOP(comment)
    }

    private fun authorAndHiddenChildrenOP(comment: Comment) =
        "${comment.author} ${resourceProvider.op()} [${comment.commentCount + 1} ${resourceProvider.hidden()}]"

    private fun authorAndHiddenChildrenNotOP(comment: Comment) =
        "${comment.author} [${comment.commentCount + 1} ${resourceProvider.hidden()}]"

    private fun comments(numComments: Int) = resourceProvider.numComments(numComments)
    private fun scoreText(score: Int) = resourceProvider.score(score)
}
