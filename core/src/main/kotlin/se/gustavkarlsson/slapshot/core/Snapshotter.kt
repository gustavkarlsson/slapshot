package se.gustavkarlsson.slapshot.core

public interface Snapshotter<T> {
    public fun snapshot(data: T)
}
