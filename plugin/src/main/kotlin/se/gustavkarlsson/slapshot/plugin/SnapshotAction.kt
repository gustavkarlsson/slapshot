package se.gustavkarlsson.slapshot.plugin

enum class SnapshotAction(val systemProperty: String) { // FIXME Action?
    CompareOnly("compareOnly"),
    CompareAndAdd("compareAndAdd"),
    Overwrite("overwrite")
}
