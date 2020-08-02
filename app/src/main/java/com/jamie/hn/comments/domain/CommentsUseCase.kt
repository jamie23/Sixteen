package com.jamie.hn.comments.domain

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.stories.repository.StoriesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class CommentsUseCase(
    private val repository: StoriesRepository
) {

    fun retrieveComments(
        scope: CoroutineScope,
        storyId: Long,
        useCache: Boolean,
        onResult: (List<CommentWithDepth>) -> Unit
    ) {
        scope.launch {
            val story = repository.story(storyId, useCache)
            val listAllComments = mutableListOf<CommentWithDepth>()

            story.comments.forEach {
                listAllComments.addAll(it.allCommentsInChain())
            }

            onResult(listAllComments)
        }
    }

    private fun Comment.allCommentsInChain(depth: Int = 0): List<CommentWithDepth> {
        if (this.comments.isEmpty()) return listOf(
            commentWithoutChildren(
                this,
                depth
            )
        )

        val listComments = mutableListOf<CommentWithDepth>()

        listComments.add(
            commentWithoutChildren(
                this,
                depth
            )
        )

        this.comments.forEach {
            listComments.addAll(it.allCommentsInChain(depth + 1))
        }

        return listComments
    }

    // Remove the embedded children comments in the object otherwise its a large duplication of data
    private fun commentWithoutChildren(
        comment: Comment,
        depth: Int
    ) =
        CommentWithDepth(
            comment = comment.copy(comments = listOf()),
            depth = depth
        )
}
