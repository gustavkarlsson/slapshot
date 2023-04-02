package se.gustavkarlsson.slapshot.plugin

enum class SnapshotAction(val systemProperty: String) {
    CompareOnly("compareOnly"),
    CompareAndAdd("compareAndAdd"),
    Overwrite("overwrite")
}
