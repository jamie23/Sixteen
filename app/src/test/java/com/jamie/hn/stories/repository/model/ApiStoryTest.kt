package com.jamie.hn.stories.repository.model

import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.InstantExecutorExtension
import com.jamie.hn.core.StoryType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import junit.framework.TestCase.assertEquals

@ExtendWith(InstantExecutorExtension::class)
class ApiStoryTest : BaseTest() {

    @Test
    fun `when comments url matches article url then return TEXT`() {
        val apiStory = generateApiStory(title = "Ask HN: Hello", url = "comments-url", commentsUrl = "comments-url")
        assertEquals(StoryType.TEXT, apiStory.storyType)
    }

    @Test
    fun `when url does not match comments url then return STANDARD`() {
        val apiStory = generateApiStory(title = "Non text story", url = "url", commentsUrl = "comments-url")
        assertEquals(StoryType.STANDARD, apiStory.storyType)
    }

    private fun generateApiStory(title: String, commentsUrl: String = "", url: String = "") = ApiStory(
        author = "jamie",
        comments = null,
        commentsUrl = commentsUrl,
        domain = "domain",
        id = 1,
        score = 2,
        time = "2016-01-14T21:33:17.000Z",
        title = title,
        url = url
    )
}
