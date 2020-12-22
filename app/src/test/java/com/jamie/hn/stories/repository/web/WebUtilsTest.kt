package com.jamie.hn.stories.repository.web

import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.StoriesType.ASK
import com.jamie.hn.core.StoriesType.JOBS
import com.jamie.hn.core.StoriesType.NEW
import com.jamie.hn.core.StoriesType.SHOW
import com.jamie.hn.core.StoriesType.TOP
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebUtilsTest : BaseTest() {

    @Test
    fun `when getWebPath is called with TOP then return top`() {
        assertEquals("top", getWebPath(TOP))
    }

    @Test
    fun `when getWebPath is called with ASK then return ask`() {
        assertEquals("ask", getWebPath(ASK))
    }

    @Test
    fun `when getWebPath is called with JOBS then return jobs`() {
        assertEquals("jobs", getWebPath(JOBS))
    }

    @Test
    fun `when getWebPath is called with NEW then return new`() {
        assertEquals("new", getWebPath(NEW))
    }

    @Test
    fun `when getWebPath is called with SHOW then return show`() {
        assertEquals("show", getWebPath(SHOW))
    }
}
