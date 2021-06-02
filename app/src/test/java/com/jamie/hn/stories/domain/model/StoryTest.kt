package com.jamie.hn.stories.domain.model

import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.StoryType
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StoryTest : BaseTest() {

    @Test
    fun `when story title starts with Ask HN then storyType returns ASK`() {
        val story = Story(title = "Ask HN:", time = DateTime.now())
        assertEquals(StoryType.ASK, story.storyType)
    }

    @Test
    fun `when story title does not start with Ask HN then storyType returns STANDARD`() {
        val story = Story(title = "Lisp - Why you should use it", time = DateTime.now())
        assertEquals(StoryType.STANDARD, story.storyType)
    }
}
