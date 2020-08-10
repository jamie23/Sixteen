package com.jamie.hn.comments.ui.repository

import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.core.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommentsViewRepositoryTest : BaseTest() {

    @MockK
    private lateinit var callback: (List<CommentCurrentState>) -> Unit

    private lateinit var repository: CommentsViewRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { callback.invoke(any()) } returns Unit

        repository = CommentsViewRepository(callback)
    }

    @Test
    fun `when commentList is updated then invoke the callback with the new view state `() {
        val commentWithDepth = mockk<CommentWithDepth>()
        val newViewState = mutableListOf(
            CommentCurrentState(commentWithDepth, FULL)
        )

        repository.commentList = newViewState

        verify { callback.invoke(newViewState) }
    }
}
