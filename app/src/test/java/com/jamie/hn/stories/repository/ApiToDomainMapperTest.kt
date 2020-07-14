package com.jamie.hn.stories.repository

import com.jamie.hn.comments.domain.model.Comment
import com.jamie.hn.core.BaseTest
import com.jamie.hn.stories.repository.model.ApiComment
import com.jamie.hn.stories.repository.model.ApiStory
import org.joda.time.DateTime
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiToDomainMapperTest : BaseTest() {

    private lateinit var apiToDomainMapper: ApiToDomainMapper
    private val time = "2016-01-14T21:33:17.000Z"

    @BeforeEach
    fun setup() {
        apiToDomainMapper = ApiToDomainMapper()
    }

    @Test
    fun `when toStoryDomainModel is called with a story with no comments then correctly return a Story with no comments`() {
        val apiStory = ApiStory(
            author = "jamie",
            comments = null,
            commentsUrl = "url",
            domain = "domain",
            id = 1,
            score = 2,
            time = time,
            title = "title",
            url = "url"
        )

        val story = apiToDomainMapper.toStoryDomainModel(apiStory)

        assertEquals("jamie", story.author)
        assertEquals(emptyList<Comment>(), story.comments)
        assertEquals("url", story.url)
        assertEquals("domain", story.domain)
        assertEquals(1, story.id)
        assertEquals(2, story.score)
        assertEquals(DateTime.parse(time), story.time)
        assertEquals("title", story.title)
        assertEquals("url", story.url)
    }

    @Test
    fun `when a nested comment count is 0 then return the single comment as part of the story`() {
        val commentList = listOf(
            ApiComment(
                author = "Jamie",
                comments = emptyList(),
                commentCount = 0,
                text = "text",
                time = time
            )
        )

        val apiStory = ApiStory(
            comments = commentList,
            commentCount = 1,
            time = time
        )

        val comments = apiToDomainMapper.toStoryDomainModel(apiStory).comments

        assertEquals(1, comments.size)
        assertEquals("Jamie", comments[0].author)
        assertEquals(0, comments[0].comments.size)
        assertEquals("text", comments[0].text)
        assertEquals(DateTime.parse(time), comments[0].time)
    }

    @Test
    fun `when a nested comment count is 1 then return a nested comment inside the top comment of the story`() {
        val nestedComment = ApiComment(
            author = "Jamie",
            comments = emptyList(),
            commentCount = 0,
            text = "text",
            time = time
        )

        val topComment = nestedComment.copy(
            comments = listOf(nestedComment),
            commentCount = 1
        )

        val apiStory = ApiStory(
            comments = listOf(topComment),
            commentCount = 1,
            time = time
        )

        val comments = apiToDomainMapper.toStoryDomainModel(apiStory).comments

        assertEquals(1, comments.size)
        assertEquals("Jamie", comments[0].author)
        assertEquals(1, comments[0].comments.size)
        assertEquals("text", comments[0].text)
        assertEquals(DateTime.parse(time), comments[0].time)

        val resultNestedCommentList = comments[0].comments

        assertEquals(1, resultNestedCommentList.size)
        assertEquals("Jamie", resultNestedCommentList[0].author)
        assertEquals(emptyList<Comment>(), resultNestedCommentList[0].comments)
        assertEquals("text", resultNestedCommentList[0].text)
        assertEquals(DateTime.parse(time), resultNestedCommentList[0].time)
    }
}
