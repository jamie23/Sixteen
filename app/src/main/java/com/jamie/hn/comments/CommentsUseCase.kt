package com.jamie.hn.comments

import com.jamie.hn.comments.net.CommentsRepository
import com.jamie.hn.core.Item
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class CommentsUseCase(
    private val commentsRepository: CommentsRepository
) {
    suspend fun getComments(item: Item): List<Comment> {

        if (item.childrenIds.isEmpty()) return emptyList()

        val deferredList = mutableListOf<Deferred<Comment>>()

        try {
            withContext(Dispatchers.IO) {
                item.childrenIds.forEach { id ->
                    deferredList.add(
                        async {
                            getComment(id)
                        })
                }
            }
        } catch (e: Exception) {
            println(e)
        }

        return deferredList.awaitAll()
    }

    private suspend fun getComment(id: Long): Comment {
        val comment = commentsRepository.comment(id)
        if (comment.childrenIds.isEmpty()) return comment
        comment.listChildComments = getComments(comment)
        return comment
    }
}
