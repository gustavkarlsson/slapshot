package se.gustavkarlsson.slapshot.core

interface Snapshotter<T> {
    fun snapshot(data: T)
}
