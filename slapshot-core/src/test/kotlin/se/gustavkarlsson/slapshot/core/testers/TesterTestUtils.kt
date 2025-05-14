package se.gustavkarlsson.slapshot.core.testers

import se.gustavkarlsson.slapshot.core.Tester
import strikt.api.expect
import strikt.assertions.isNotNull
import strikt.assertions.isNull

fun <T> tableTestValuesPassing(
    table: List<Pair<T, T>>,
    tester: Tester<T>,
) {
    expect {
        for ((actual, expected) in table) {
            that(tester).get("test($actual, $expected)") { test(actual, expected) }.isNull()
        }
    }
}

fun <T> tableTestValuesFailing(
    table: List<Pair<T, T>>,
    tester: Tester<T>,
) {
    expect {
        for ((actual, expected) in table) {
            that(tester).get("test($actual, $expected)") { test(actual, expected) }.isNotNull()
        }
    }
}
