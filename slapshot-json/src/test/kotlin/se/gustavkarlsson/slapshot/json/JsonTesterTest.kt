package se.gustavkarlsson.slapshot.json

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.isNotNull
import strikt.assertions.isNull

class JsonTesterTest {
    private val tester = JsonTester()
    private val testerJUnitStyle = JsonTester(errorStyle = JsonErrorStyle.JUnitStyle)

    @Test
    fun `test matching JSON strings return null`() {
        val table =
            listOf(
                "{ }" to "{}",
                "[]" to "[ ]",
                """ { "key" : "value" } """ to """{"key":"value"}""",
                """{"a":1,"b":2}""" to """ { "a" : 1 , "b" : 2 }""",
                " [ 1 , 2 , 3 ] " to "[1,2,3]",
                """{"nested":{"key":"value"}}""" to """{"nested":{"key":"value"}}""",
                """{"array":[1,2,3]}""" to """{"array":[1,2,3]}""",
                " true " to "true",
                "false" to " false ",
                " null" to "null ",
                "123 " to " 123",
                """"string" """ to """ "string"""",
            )

        tableTestValuesPassing(table, tester)
    }

    @Test
    fun `test reordering of object keys`() {
        val result = tester.test("""{"a":1,"b":2}""", """{"b":2,"a":1}""")
        expectThat(result).isNull()
    }

    @Test
    fun `test mismatching JSON strings return error message`() {
        val table =
            listOf(
                """{"key":"value"}""" to """{"key":"different"}""",
                """{"a":1,"b":2}""" to """{"a":1,"b":3}""",
                """[1,2,3]""" to """[1,2,4]""",
                """{"nested":{"key":"value"}}""" to """{"nested":{"key":"different"}}""",
                """{"array":[1,2,3]}""" to """{"array":[1,2,4]}""",
                """true""" to """false""",
                """123""" to """456""",
                """"string"""" to """"different"""",
            )

        tableTestValuesFailing(table, tester)
    }

    @Test
    fun `test type mismatch returns error message`() {
        val table =
            listOf(
                """{"key":"value"}""" to """["key","value"]""",
                """[1,2,3]""" to """{"array":[1,2,3]}""",
                """true""" to """1""",
                """123""" to """"123"""",
                """null""" to """false""",
            )

        tableTestValuesFailing(table, tester)
    }

    @Test
    fun `test error message includes type difference`() {
        val actual = """{"key":123}"""
        val expected = """{"key":"123"}"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Expected $.key to be a <string> but it was a <number>")
    }

    @Test
    fun `test error message includes value difference`() {
        val actual = """{"key":"actual"}"""
        val expected = """{"key":"expected"}"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Expected $.key to be <\"expected\"> but it was <\"actual\">")
    }

    @Test
    fun `test error message includes missing keys`() {
        val actual = """{"key1":"value1"}"""
        val expected = """{"key1":"value1","key2":"value2"}"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Expected an element at $.key2")
    }

    @Test
    fun `test error message includes added keys`() {
        val actual = """{"key1":"value1","key2":"value2"}"""
        val expected = """{"key1":"value1"}"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Unexpected element at $.key2")
    }

    @Test
    fun `test error message includes array length difference`() {
        val actual = """[1,2,3]"""
        val expected = """[1,2]"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Expected $ to have a length of <2> but it had a length of <3>")
    }

    @Test
    fun `test error message includes array element difference`() {
        val actual = """[1,2,3]"""
        val expected = """[1,5,3]"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Expected $[1] to be <5> but it was <2>")
    }

    @Test
    fun `test nested object differences`() {
        val actual = """{"nested":{"key":"actual"}}"""
        val expected = """{"nested":{"key":"expected"}}"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Expected $.nested.key to be <\"expected\"> but it was <\"actual\">")
    }

    @Test
    fun `test nested array differences`() {
        val actual = """{"array":[1,{"key":"actual"},3]}"""
        val expected = """{"array":[1,{"key":"expected"},3]}"""

        val result = tester.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("Expected $.array[1].key to be <\"expected\"> but it was <\"actual\">")
    }

    @Test
    fun `test junit style error message`() {
        val actual = """{"key":"actual"}"""
        val expected = """{"key":"expected"}"""

        val result = testerJUnitStyle.test(actual, expected)

        expectThat(result).isNotNull()
            .contains("expected: <")
            .contains("> but was: <")
            .contains(">")
    }

    @Test
    fun `test invalid JSON throws exception`() {
        val list =
            listOf(
                "a",
                "[",
                "]",
                "(",
                ")",
                "",
                "[five]",
                """{"invalid": json}""",
                """{invalid: "json"}""",
                """[null}""",
            )

        for (value in list) {
            expectThrows<IllegalArgumentException> { tester.test(value, value) }
        }
    }
}
