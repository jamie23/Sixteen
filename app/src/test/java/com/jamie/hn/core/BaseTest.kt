package com.jamie.hn.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
open class BaseTest {

    @BeforeEach
    fun initialise() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }
}
