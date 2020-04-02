package com.jamie.hn.comments.net

import com.jamie.hn.core.net.HackerNewsService

class CommentsRepository(
    private val service: HackerNewsService
) {
    suspend fun comment(id: Long) = service.getComment(id)
}
