package com.jamie.hn.core.ui

import com.jamie.hn.core.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoreDataMapperTest : BaseTest() {

    @MockK
    private lateinit var coreResourceProvider: CoreResourceProvider

    private lateinit var coreDataMapper: CoreDataMapper

    @BeforeEach
    private fun setup() {
        MockKAnnotations.init(this)

        every { coreResourceProvider.days } returns "d"
        every { coreResourceProvider.hours } returns "h"
        every { coreResourceProvider.minutes } returns "m"

        coreDataMapper = CoreDataMapper(coreResourceProvider)
    }

    @Nested
    inner class Time {

        @Test
        fun `when time is yesterday then use days`() {
            val dateDay = DateTime.now().minusDays(1)

            val item = coreDataMapper.time(dateDay)
            Assertions.assertEquals("1d", item)
        }

        @Test
        fun `when time is hour ago then use hours`() {
            val dateHour = DateTime.now().minusHours(1)

            println(dateHour)
            val item = coreDataMapper.time(dateHour)
            Assertions.assertEquals("1h", item)
        }

        @Test
        fun `when time is minutes ago then use minutes`() {
            val dateMinute = DateTime.now().minusMinutes(1)

            val item = coreDataMapper.time(dateMinute)
            Assertions.assertEquals("1m", item)
        }

        @Test
        fun `when time is outside of checked range then use empty string`() {
            val dateNow = DateTime.now()

            val item = coreDataMapper.time(dateNow)
            Assertions.assertEquals("", item)
        }
    }
}
