package com.jamie.hn.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.BeforeEach

open class BaseTest {

    @BeforeEach
    fun initialise() {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }
}