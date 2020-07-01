package com.jamie.hn.ui

import com.jamie.hn.core.ui.CoreDataMapper
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach

class CommentDataMapperTest {

    @MockK
    private lateinit var coreDataMapper: CoreDataMapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }
}
