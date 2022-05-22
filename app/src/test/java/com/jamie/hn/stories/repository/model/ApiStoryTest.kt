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
    fun `when storyType starts with Ask HN then return ASK`() {
        val apiStory = generateApiStory(title = "Ask HN: Hello")
        assertEquals(StoryType.ASK, apiStory.storyType)
    }

    @Test
    fun `when storyType starts with Tell HN then return ASK`() {
        val apiStory = generateApiStory(title = "Tell HN: Hello")
        assertEquals(StoryType.ASK, apiStory.storyType)
    }

    @Test
    fun `when storyType starts with anything else then return STANDARD`() {
        val apiStory = generateApiStory(title = "Non ask story")
        assertEquals(StoryType.STANDARD, apiStory.storyType)
    }

    private fun generateApiStory(title: String) = ApiStory(
        author = "jamie",
        comments = null,
        commentsUrl = "url",
        domain = "domain",
        id = 1,
        score = 2,
        time = "2016-01-14T21:33:17.000Z",
        title = title,
        url = "url"
    )
}
