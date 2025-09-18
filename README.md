# Slapshot - A snapshot testing library for Kotlin

[![Checks](https://github.com/gustavkarlsson/slapshot/actions/workflows/checks.yaml/badge.svg)](https://github.com/gustavkarlsson/slapshot/actions/workflows/checks.yaml)

## About snapshot testing

Snapshot testing is a type of regression testing where the output some code (such as a function, UI component, or
API response) is captured and stored as a "snapshot." This snapshot serves as a reference for future test runs. During
subsequent tests, the new output is compared to the stored snapshot to ensure that the behavior of the code has not
changed unexpectedly.

## How Slapshot works

Slapshot is a Kotlin library with an accompanying Gradle Plugin that integrates with test frameworks
such as JUnit 4 and 5.

You create unit tests as per usual, but instead of assertions you call `snapshotter.snapshot(data)` on some piece of data.
The data will be compared to the previously saved data and fail the test if they differ.

With an extensible architecture, Slapshot enables you to snapshot test any data that can be serialized.

Some examples of built-in type support:

* Primitives such as numbers and strings
* Collections like lists, sets, and maps
* Java time types, such as instants, durations, and dates
* JSON data

But you can also implement your own serializer to suit your own needs.

## Getting Started

### Add the plugin

In your `build.gradle.kts` file:

```kotlin
plugins {
    id("se.gustavkarlsson.slapshot") version "<latest_version>"
}

// Optional config block
slapshot {
    testFramework.set(TestFramework.JUnit5) // Default
}
```

### Set up a test class with a snapshotter

**JUnit 5**

```kotlin
@ExtendWith(SnapshotExtension::class)
class MyTests {
    private lateinit var snapshotter: Snapshotter<String>

    @BeforeEach
    fun initSnapshotContext(snapshotContext: JUnit5SnapshotContext) {
        // Configure the snapshotter for your purposes
        snapshotter = snapshotContext.createSnapshotter(StringSerializer())
    }
}
```

**JUnit 4**

```kotlin
class MyTests {
    @get:Rule
    val snapshotContext = JUnit4SnapshotContext()
    val snapshotter = let {
        // Configure the snapshotter for your purposes
        snapshotContext.createSnapshotter(StringSerializer())
    }
}
```

### Write a test

Here is an example string manipulation test:

```kotlin
class MyTests {
    @Test
    fun `test string`() {
        val result = reverseString("foobar")
        snapshotter.snapshot(result)
    }
}
```

### Run the test

The first time a new test runs, there will be no snapshot to compare against. The test will fail and save a snapshot
file with the results for future reference.

The next time you run the test, it will compare the result with the saved snapshot and succeed!

### Commit the snapshots to source control

By committing the snapshot files, we are preventing regressions that would cause the results to change.

## Snapshotting different types of data

Slapshot is not limited to strings! Different data types are supported through different implementations of the
`Serializer` interface.

Slapshot comes with many serializers out of the box. Check them out or implement your own!

## Testing complex data

By default, Slapshot compares snapshots by equality, but sometimes more sophisticated comparisons are needed.
For example, floating point types and lossy data types might need to be compared with some degree of tolerance.
Large data structures such as JSON or graphs could benefit from pointing out the location of the mismatch.

Slapshot uses a `Tester` interface to handle comparisons and produce error messages.
Some are included, but you can easily implement your own.

## Extension libraries

The project includes additional libraries to extend its capabilities.

Add them as test dependencies like this:

```kotlin
dependencies {
    // Omit the version. The Slapshot Gradle plugin sets the correct one.
    testImplementation("se.gustavkarlsson.slapshot:slapshot-json") // Create and test JSON snapshots
    testImplementation("se.gustavkarlsson.slapshot:slapshot-ktor3") // Test ktor requests and responses (see sample)
}
```

## Configuration

Slapshot can be configured in different ways.

### build.gradle.kts

The plugin can be configured with an extension block:

```kotlin
slapshot {
    testFramework.set(TestFramework.JUnit4) // JUnit5 is default
    snapshotRootDir.set("my/awesome/snapshots") // Where snapshots are stored
    snapshotAction.set(SnapshotAction.CompareOnly) // Change how snapshots are handled
}
```

### Gradle properties

Some settings can be overridden with
[Gradle project properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties).

```shell
./gradlew test -PsnapshotRootDir="my/snapshots" -PsnapshotAction=compareOnly
```

`-PsnapshotAction=compareOnly` is recommended for CI environments to avoid creating new files.

### In runtime

When you create a `Snapshotter` from a `SnapshotContext`, you can override some configuration for each snapshotter instance.

```kotlin
val snapshotter = snapshotContext.createSnapshotter(
    serializer = LongSerializer,
    overrideSnapshotFileResolver = { rootDirectory, testInfo, fileExtension ->
        val directory = rootDirectory.resolve(testInfo.testClass.get().name)
        val fileName = "${testInfo.displayName}_snapshot.$fileExtension"
        directory.resolve(fileName)
    },
    overrideAction = SnapshotAction.Overwrite,
)
```

## Deleting all snapshots

If you want to start over, you can nuke all snapshots by running:

```shell
./gradlew clearSnapshots
```

This can be useful if you have changed how or where snapshots are stored and want a "fresh start."

## Sample projects

Check out the sample projects to see how to integrate Slapshot in different types of projects.

To run them, you have to first publish a local version of the plugin.

```shell
./gradlew check publishToMavenLocal
./gradlew sample-junit4:check sample-junit5:check sample-ktor:check --include-build sample-junit4 --include-build sample-junit5 --include-build sample-ktor
```
