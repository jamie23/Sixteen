package com.jamie.hn.comments.ui

import android.content.res.Resources
import com.jamie.hn.core.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CommentsResourceProviderTest : BaseTest() {

    @MockK
    private lateinit var resources: Resources

    private lateinit var commentsResourceProvider: CommentsResourceProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { resources.getString(any()) } returns "children"
        commentsResourceProvider = CommentsResourceProvider(resources)
    }

    @Test
    fun `when children is called then return children string from resources`() {
        val returnedString = commentsResourceProvider.children()

        assertEquals("children", returnedString)
        verify { resources.getString(any()) }
    }
}
