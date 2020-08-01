package com.jamie.hn.comments.ui

import androidx.lifecycle.Observer
import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.CommentsListViewModel.ListViewState
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.InstantExecutorExtension
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.repository.StoriesRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class CommentsListViewModelTest : BaseTest() {

    @MockK
    private lateinit var commentDataMapper: CommentDataMapper

    @MockK
    private lateinit var storiesRepository: StoriesRepository

    private lateinit var commentsListViewModel: CommentsListViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        commentsListViewModel = CommentsListViewModel(
            commentDataMapper = commentDataMapper,
            repository = storiesRepository,
            storyId = 1
        )
    }

    @Test
    fun `when init is called then we emit refreshing view state, retrieve the story from the repository and post the new view state`() {
        val commentViewItem = mockk<CommentViewItem>()

        coEvery { storiesRepository.story(1, false) } returns Story(time = DateTime.now())
        every { commentDataMapper.toCommentViewItem(any()) } returns commentViewItem

        val observer = spyk<Observer<ListViewState>>()

        commentsListViewModel.commentsViewState().observeForever(observer)
        commentsListViewModel.init()

        coVerifySequence {
            observer.onChanged(ListViewState(emptyList(), true))
            storiesRepository.story(1, false)
            observer.onChanged(ListViewState(emptyList(), false))
        }
    }

    @Nested
    inner class RefreshList {

        @Test
        fun `when init is called then we emit refreshing view state, retrieve the story from the repository not using cache`() {
            coEvery { storiesRepository.story(1, false) } returns Story(time = DateTime.now())

            val observer = spyk<Observer<ListViewState>>()

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.refreshList()

            coVerifyOrder {
                observer.onChanged(ListViewState(emptyList(), true))
                storiesRepository.story(1, false)
            }
        }

        @Nested
        inner class AllCommentsInChain {

            @Test
            fun `when the comment has no child comments then return a list containing only that comment with correct depth`() {
                val commentWithDepth = slot<CommentWithDepth>()

                every { commentDataMapper.toCommentViewItem(capture(commentWithDepth)) } returns mockk()
                coEvery {
                    storiesRepository.story(
                        1,
                        false
                    )
                } returns story(singleComment())

                commentsListViewModel.refreshList()

                assertEquals(0, commentWithDepth.captured.depth)
                assertEquals(singleComment(), commentWithDepth.captured.comment)
            }

            @Test
            fun `when comment has multiple child comments then add return the list containing those with their correct depth `() {
                val commentWithDepth = mutableListOf<CommentWithDepth>()

                every { commentDataMapper.toCommentViewItem(capture(commentWithDepth)) } returns mockk()
                coEvery {
                    storiesRepository.story(
                        1,
                        false
                    )
                } returns story(singleCommentNestedComment())

                commentsListViewModel.refreshList()

                assertEquals(0, commentWithDepth[0].depth)
                assertEquals(singleCommentNestedComment(), commentWithDepth[0].comment)
                assertEquals(1, commentWithDepth[1].depth)
                assertEquals(singleComment(), commentWithDepth[1].comment)
            }
        }

        @Test
        fun `when we post the new state then we map the CommentWithDepth to CommentViewItems and refreshing as false`() {
            val commentWithDepth = CommentWithDepth(comment = singleComment(), depth = 0)
            val commentViewItem = CommentViewItem(
                author = "jamie",
                text = "text",
                time = "time",
                depth = 0,
                showTopDivider = false
            )
            val observer = spyk<Observer<ListViewState>>()

            every { commentDataMapper.toCommentViewItem(commentWithDepth) } returns commentViewItem
            coEvery {
                storiesRepository.story(
                    1,
                    false
                )
            } returns story(singleComment())

            commentsListViewModel.commentsViewState().observeForever(observer)
            commentsListViewModel.refreshList()

            verify {
                observer.onChanged(
                    ListViewState(
                        listOf(commentViewItem),
                        refreshing = false
                    )
                )
            }
        }
    }

    private fun story(comment: Comment) =
        Story(
            comments = listOf(comment),
            time = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
        )

    private fun singleCommentNestedComment() =
        singleComment().copy(comments = listOf(singleComment()), commentCount = 1)

    private fun singleComment() =
        Comment(
            author = "Jamie",
            comments = emptyList(),
            commentCount = 0,
            text = "text",
            time = DateTime.parse(
                "23/08/2020 09:00:00",
                DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
            )
        )
}
