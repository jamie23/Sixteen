package com.jamie.hn.comments.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.hn.comments.domain.CommentsUseCase
import com.jamie.hn.comments.domain.model.CommentWithDepth
import com.jamie.hn.comments.ui.CommentsListViewModel.ShareChoice.ARTICLE_COMMENTS
import com.jamie.hn.comments.ui.CommentsListViewModel.ShareChoice.ARTICLE
import com.jamie.hn.comments.ui.CommentsListViewModel.ShareChoice.COMMENTS
import com.jamie.hn.comments.ui.CommentsListViewModel.SortChoice.NEWEST
import com.jamie.hn.comments.ui.CommentsListViewModel.SortChoice.STANDARD
import com.jamie.hn.comments.ui.CommentsListViewModel.SortChoice.OLDEST
import com.jamie.hn.comments.ui.repository.CommentsViewRepository
import com.jamie.hn.comments.ui.repository.model.CommentCurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState
import com.jamie.hn.comments.ui.repository.model.CurrentState.COLLAPSED
import com.jamie.hn.comments.ui.repository.model.CurrentState.FULL
import com.jamie.hn.comments.ui.repository.model.CurrentState.HIDDEN
import com.jamie.hn.core.Event
import com.jamie.hn.stories.domain.StoriesUseCase
import com.jamie.hn.stories.domain.model.Story
import com.jamie.hn.stories.ui.StoryListViewModel
import kotlinx.coroutines.launch

class CommentsListViewModel(
    private val commentDataMapper: CommentDataMapper,
    private val storyId: Int,
    private val commentsUseCase: CommentsUseCase,
    private val storiesUseCase: StoriesUseCase,
    private val commentsResourceProvider: CommentsResourceProvider
) : ViewModel() {

    private val listViewState = MutableLiveData<ListViewState>()
    fun commentsViewState(): LiveData<ListViewState> = listViewState

    private val networkErrorCachedResults = MutableLiveData<Event<Unit>>()
    fun networkErrorCachedResults(): LiveData<Event<Unit>> = networkErrorCachedResults

    private val networkErrorNoCacheResults = MutableLiveData<Event<Unit>>()
    fun networkErrorNoCacheResults(): LiveData<Event<Unit>> = networkErrorNoCacheResults

    private val urlClicked = MutableLiveData<String>()
    fun urlClicked(): LiveData<String> = urlClicked

    private val navigateToArticle = MutableLiveData<Event<String>>()
    fun navigateToArticle(): LiveData<Event<String>> = navigateToArticle

    private val articleTitle = MutableLiveData<String>()
    fun articleTitle(): LiveData<String> = articleTitle

    private val shareUrl = MutableLiveData<Event<String>>()
    fun shareUrl(): LiveData<Event<String>> = shareUrl

    private val sortState = MutableLiveData(0)
    fun sortState(): LiveData<Int> = sortState

    private lateinit var commentsViewRepository: CommentsViewRepository
    private lateinit var story: Story

    fun userManuallyRefreshed() {
        refreshList(false)
    }

    fun automaticallyRefreshed() {
        refreshList(true)
    }

    fun init() {
        commentsViewRepository =
            CommentsViewRepository(
                ::viewStateUpdate
            )

        viewModelScope.launch {
            story = storiesUseCase.getStory(storyId, true).story
            updateTitleWithArticleTitle()
        }

        automaticallyRefreshed()
    }

    private fun updateTitleWithArticleTitle() {
        articleTitle.value = story.title
    }

    private fun refreshList(useCachedVersion: Boolean) {
        listViewState.value = ListViewState(
            comments = emptyList(),
            refreshing = true
        )

        viewModelScope.launch {
            commentsUseCase.retrieveComments(
                storyId = storyId,
                useCache = useCachedVersion,
                onResult = ::populateUiCommentRepository,
                requireComments = true
            )
        }
    }

    private fun viewStateUpdate(commentList: List<CommentCurrentState>) {
        listViewState.value =
            ListViewState(commentsToViewItems(commentList.filter { it.state != HIDDEN }), false)
    }

    private fun commentsToViewItems(comments: List<CommentCurrentState>): List<ViewItem> {
        val viewStateComments = comments.map {
            commentDataMapper.toCommentViewItem(
                it,
                ::longClickCommentListener,
                ::urlClicked
            )
        }

        return addHeader(viewStateComments)
    }

    fun openArticle() {
        navigateToArticle.value = Event(story.url)
    }

    // Transform list from API to a list with UI state, all items initialised with FULL state shown
    private fun populateUiCommentRepository(
        listAllComments: List<CommentWithDepth>,
        networkFailure: Boolean,
        useCachedVersion: Boolean
    ) {

        if (listAllComments.isEmpty() && networkFailure) {
            networkErrorNoCacheResults.value = Event(Unit)
            return
        }

        if (listAllComments.isNotEmpty() && networkFailure && !useCachedVersion) {
            networkErrorCachedResults.value = Event(Unit)
        }

        val sortedList = sortList(listAllComments)

        commentsViewRepository.commentList = sortedList.mapIndexed { index, commentWithDepth ->
            CommentCurrentState(comment = commentWithDepth.copy(id = index), state = FULL)
        }
    }

    private fun sortList(listAllComments: List<CommentWithDepth>): List<CommentWithDepth> {
        // Returns a list containing each top level parent and its index sorted by users choice
        // The list comes sorted so the index holds the state of the sorted list from the server
        val sortedListTopLevelComments =
            listAllComments
                .withIndex()
                .filter { it.value.depth == 0 }
                .sortedWith(
                    when (getSortEnum(sortState.value ?: -1)) {
                        STANDARD -> sortByServerOrder()
                        NEWEST -> sortByOldestTime().reversed()
                        OLDEST -> sortByOldestTime()
                        else -> throw IllegalArgumentException("Erroneous sort option chosen")
                    }
                )

        val sortedFullList = mutableListOf<CommentWithDepth>()

        sortedListTopLevelComments.forEach {
            sortedFullList.add(it.value)
            if (addChildren(it.value)) {
                val children = listAllComments.subList(it.index + 1, (it.index + 1) + it.value.comment.commentCount)
                sortedFullList.addAll(children)
            }
        }

        return sortedFullList
    }

    private fun getSortEnum(order: Int) =
        when (order) {
            0 -> STANDARD
            1 -> NEWEST
            2 -> OLDEST
            else -> throw IllegalArgumentException("Erroneous sort option chosen")
        }

    private fun sortByOldestTime() =
        compareBy<IndexedValue<CommentWithDepth>> { it.value.comment.time }

    // Repository stores the list in the server sorted time, index here represents the sort order from the server
    private fun sortByServerOrder() =
        compareBy<IndexedValue<CommentWithDepth>> { it.index }

    private fun addChildren(comment: CommentWithDepth) =
        comment.comment.commentCount > 0

    private fun addHeader(listAllComments: List<ViewItem>): List<ViewItem> {
        val headerItem = commentDataMapper.toStoryHeaderViewItem(
            story = story,
            storyViewerCallback = ::articleViewerCallback
        )

        // Place the header item at the start of a new list followed by comments
        return listOf(headerItem) + listAllComments
    }

    private fun articleViewerCallback() {
        openArticle()
    }

    private fun longClickCommentListener(id: Int) {
        val newStateList = commentsViewRepository.commentList.toMutableList()
        val commentWithState = newStateList[id]
        val childrenNewState: CurrentState

        if (commentWithState.state == FULL) {
            commentWithState.state = COLLAPSED
            childrenNewState = HIDDEN
        } else {
            commentWithState.state = FULL
            childrenNewState = FULL
        }

        updateVisibilityStateOfItem(
            childrenNewState,
            commentWithState,
            newStateList,
            commentWithState.comment.depth,
            id
        )

        commentsViewRepository.commentList = newStateList
    }

    private fun updateVisibilityStateOfItem(
        newState: CurrentState,
        commentWithState: CommentCurrentState,
        newStateList: MutableList<CommentCurrentState>,
        depth: Int,
        id: Int
    ) {
        if (commentWithState.comment.id != newStateList.lastIndex) {
            val idOfNextSibling = newStateList.subList(id + 1, newStateList.size)
                .firstOrNull { it.comment.depth <= depth }?.comment?.id ?: newStateList.size

            for (i in id + 1 until idOfNextSibling) {
                newStateList[i].state = newState
            }
        }
    }

    private fun urlClicked(url: String) {
        urlClicked.value = url
    }

    fun share(item: Int) {
        shareUrl.value = when (getShareEnum(item)) {
            ARTICLE -> Event(getShareArticleText())
            COMMENTS -> Event(getShareCommentsText())
            ARTICLE_COMMENTS -> Event(getShareArticleCommentsText())
        }
    }

    private fun getShareArticleText() =
        "${story.title} - ${story.url}"

    private fun getShareCommentsText() =
        "${story.title} - ${COMMENTS_URL}${story.id}"

    private fun getShareArticleCommentsText() =
        """${story.title}
            |${commentsResourceProvider.article()} - ${story.url}
            |${commentsResourceProvider.comments()} - ${COMMENTS_URL}${story.id}""".trimMargin()

    private fun getShareEnum(order: Int) =
        when (order) {
            0 -> ARTICLE
            1 -> COMMENTS
            2 -> ARTICLE_COMMENTS
            else -> throw IllegalArgumentException("Erroneous share option chosen")
        }

    fun updateSortState(which: Int) {
        sortState.value = which
    }

    enum class SortChoice {
        STANDARD, NEWEST, OLDEST
    }

    enum class ShareChoice {
        ARTICLE, COMMENTS, ARTICLE_COMMENTS
    }

    data class ListViewState(
        val comments: List<ViewItem>,
        val refreshing: Boolean
    )

    companion object {
        private const val COMMENTS_URL = "https://news.ycombinator.com/item?id="
    }
}
