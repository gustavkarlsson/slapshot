package se.gustavkarlsson.slapshot.core.testers

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.isNotNull

class LongStringTesterTest {
    private val defaultTester = LongStringTester()
    private val shortDiffLengthTester = LongStringTester(diffLengthToPrint = 10)

    @Test
    fun `test matching strings return null`() {
        val table =
            listOf(
                "" to "",
                "abc" to "abc",
                "Hello, world!" to "Hello, world!",
                "Multi\nline\nstring" to "Multi\nline\nstring",
            )

        tableTestValuesPassing(table, defaultTester)
    }

    @Test
    fun `test mismatching strings return error message`() {
        val table =
            listOf(
                "abc" to "def",
                "Hello, world!" to "Hello, World!",
                "Multi\nline\nstring" to "Multi\nline\nString",
            )

        tableTestValuesFailing(table, defaultTester)
    }

    @Test
    fun `test error message includes position`() {
        val actual = "Hello\nto the world!"
        val expected = "Hello\nto the World!"

        val result = defaultTester.test(actual, expected)

        expectThat(result).isNotNull().contains("2:8")
    }

    @Test
    fun `test error message includes mismatching text`() {
        val actual = "Hello, world!"
        val expected = "Hello, World!"

        val result = defaultTester.test(actual, expected)

        expectThat(result).isNotNull().and {
            contains("<W>")
            contains("<w>")
        }
    }

    @Test
    fun `test strings differing at beginning`() {
        val actual = "Different start but same end"
        val expected = "Changed start but same end"

        val result = defaultTester.test(actual, expected)

        expectThat(result).isNotNull().and {
            contains("1:1")
            contains("<Changed>")
            contains("<Different>")
        }
    }

    @Test
    fun `test strings differing at end`() {
        val actual = "Same start but different END"
        val expected = "Same start but different stop"

        val result = defaultTester.test(actual, expected)

        expectThat(result).isNotNull().and {
            contains("1:26")
            contains("<END>")
            contains("<stop>")
        }
    }

    @Test
    fun `test diffLengthToPrint limits output`() {
        val actual = "This string has a very long difference that should be truncated"
        val expected = "This string has a completely different text that should be truncated"

        val result = shortDiffLengthTester.test(actual, expected)

        expectThat(result).isNotNull().and {
            contains("<very long >…")
            contains("<completely>…")
        }
    }

    @Test
    fun `test constructor with non-positive diffLengthToPrint`() {
        expectThrows<IllegalArgumentException> {
            LongStringTester(diffLengthToPrint = 0)
        }
    }
}
