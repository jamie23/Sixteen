package com.jamie.hn.core.ui

import com.jamie.hn.core.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

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
        // TODO: CHANGE THESE TO PARSE DATE
        @Test
        fun `when time is yesterday then use days`() {
            val dateYesterday = LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)

            val item = coreDataMapper.time(dateYesterday)
            Assertions.assertEquals("1d", item)
        }

        @Test
        fun `when time is hour ago then use hours`() {
            val dateHour = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.UTC)

            val item = coreDataMapper.time(dateHour)
            Assertions.assertEquals("1h", item)
        }

        @Test
        fun `when time is minutes ago then use minutes`() {
            val dateMinute = LocalDateTime.now().minusMinutes(1).toEpochSecond(ZoneOffset.UTC)

            val item = coreDataMapper.time(dateMinute)
            Assertions.assertEquals("1m", item)
        }

        @Test
        fun `when time is outside of checked range then use empty string`() {
            val dateNow = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

            val item = coreDataMapper.time(dateNow)
            Assertions.assertEquals("", item)
        }
    }
}
