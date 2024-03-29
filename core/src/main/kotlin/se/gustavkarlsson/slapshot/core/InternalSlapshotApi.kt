package se.gustavkarlsson.slapshot.core

@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal API that " +
        "should not be used from outside of slapshot. No compatibility guarantees are provided."
)
public annotation class InternalSlapshotApi
