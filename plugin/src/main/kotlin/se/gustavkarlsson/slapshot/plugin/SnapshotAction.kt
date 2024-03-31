package se.gustavkarlsson.slapshot.plugin

public enum class SnapshotAction(public val systemProperty: String) {
    CompareOnly("compareOnly"),
    CompareAndAdd("compareAndAdd"),
    Overwrite("overwrite"),
}
