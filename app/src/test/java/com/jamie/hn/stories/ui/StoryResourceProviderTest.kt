package com.jamie.hn.stories.ui

import android.content.res.Resources
import com.jamie.hn.core.BaseTest
import com.jamie.hn.core.InstantExecutorExtension
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class StoryResourceProviderTest : BaseTest() {

    @MockK
    private lateinit var resources: Resources

    private lateinit var storyResourceProvider: StoryResourceProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { resources.getQuantityString(any(), any(), any()) } returns "quantity"
        storyResourceProvider = StoryResourceProvider(resources)
    }

    @Test
    fun `when comments is called then get quantity string with correct number`() {
        storyResourceProvider.comments(4)

        verify { resources.getQuantityString(any(), 4, 4) }
    }
}
