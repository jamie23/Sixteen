package com.jamie.hn.stories.domain.model

import com.jamie.hn.core.BaseTest
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StoryTest : BaseTest() {

    @Test
    fun `when story title starts with Ask HN then isAskStory returns true`() {
        val story = Story(title = "Ask HN:", time = DateTime.now())
        assertTrue(story.isAskStory)
    }

    @Test
    fun `when story title does not start with Ask HN then isAskStory returns false`() {
        val story = Story(title = "Lisp - Why you should use it", time = DateTime.now())
        assertFalse(story.isAskStory)
    }
}
